package kindl.domain.auth.controller

import com.jayway.jsonpath.JsonPath
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kindl.domain.auth.model.RegistrationInfo
import kindl.domain.auth.repository.RegistrationStore
import kindl.domain.user.entity.OauthProvider
import kindl.domain.user.repository.UserRepository
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Primary
import org.springframework.http.MediaType
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.sql.Timestamp
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(
    properties = [
        "spring.datasource.url=jdbc:h2:mem:kindl;MODE=MySQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
    ],
)
@Import(
    OauthAuthIntegrationTest.StubDecoderConfig::class,
    OauthAuthIntegrationTest.InMemoryRegistrationStoreConfig::class,
)
class OauthAuthIntegrationTest(
    private val mockMvc: MockMvc,
    private val userRepository: UserRepository,
    private val jdbcTemplate: JdbcTemplate,
) : DescribeSpec({

    fun login(provider: String, idToken: String = "stub"): ResultActions =
        mockMvc.perform(
            post("/api/v1/auth/login/$provider")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"idToken":"$idToken"}"""),
        )

    fun signup(registrationKey: String, nickname: String, termsAgreed: Boolean): ResultActions =
        mockMvc.perform(
            post("/api/v1/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """{"registrationKey":"$registrationKey","nickname":"$nickname","termsAgreed":$termsAgreed}""",
                ),
        )

    fun registrationKeyByLogin(provider: String): String =
        JsonPath.read(login(provider).andReturn().response.contentAsString, "$.data.registrationKey")

    beforeTest { jdbcTemplate.execute("delete from users") }

    describe("소셜 로그인/회원가입") {
        it("미가입 유저 로그인은 202 + registrationKey를 반환한다") {
            login("google")
                .andExpect(status().isAccepted)
                .andExpect(jsonPath("$.data.registrationKey").isNotEmpty)
        }

        it("회원가입하면 201 + 토큰을 발급하고 유저를 생성한다") {
            val registrationKey = registrationKeyByLogin("google")

            signup(registrationKey, "길동", true)
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.data.accessToken.token").isNotEmpty)

            userRepository.findByProviderAndProviderId(OauthProvider.GOOGLE, "google-sub-1").shouldNotBeNull()
        }

        it("가입된 유저는 로그인시 200 + 토큰을 발급한다") {
            signup(registrationKeyByLogin("google"), "길동", true).andExpect(status().isCreated)

            login("google")
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.data.accessToken.token").isNotEmpty)
        }

        it("약관 미동의 회원가입은 400") {
            signup(registrationKeyByLogin("google"), "길동", false)
                .andExpect(status().isBadRequest)
        }

        it("유효하지 않은 registrationKey 회원가입은 400(RNF)") {
            signup(UUID.randomUUID().toString(), "길동", true)
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.code").value("RNF"))
        }

        it("같은 registrationKey는 한 번만 사용할 수 있다") {
            val registrationKey = registrationKeyByLogin("google")

            signup(registrationKey, "길동", true).andExpect(status().isCreated)
            signup(registrationKey, "길동", true)
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.code").value("RNF"))
        }

        it("서로 다른 registrationKey가 같은 소셜 계정을 가리켜도 유저는 하나만 생성된다") {
            val firstRegistrationKey = registrationKeyByLogin("google")
            val secondRegistrationKey = registrationKeyByLogin("google")

            signup(firstRegistrationKey, "첫 가입", true).andExpect(status().isCreated)
            signup(secondRegistrationKey, "재시도", true).andExpect(status().isCreated)

            userRepository.count() shouldBe 1L
        }

        it("서로 다른 registrationKey로 동시에 가입해도 두 요청 모두 성공하고 유저는 하나다") {
            val registrationKeys = listOf(registrationKeyByLogin("google"), registrationKeyByLogin("google"))
            val executorService = Executors.newFixedThreadPool(2)
            val readyLatch = CountDownLatch(2)
            val startLatch = CountDownLatch(1)

            try {
                val signupResponseStatuses = registrationKeys.mapIndexed { requestIndex, registrationKey ->
                    executorService.submit<Int> {
                        readyLatch.countDown()
                        startLatch.await(5, TimeUnit.SECONDS)
                        signup(registrationKey, "동시-$requestIndex", true).andReturn().response.status
                    }
                }
                readyLatch.await(5, TimeUnit.SECONDS)
                startLatch.countDown()

                signupResponseStatuses.map { responseStatus ->
                    responseStatus.get(10, TimeUnit.SECONDS)
                } shouldBe listOf(201, 201)
                userRepository.count() shouldBe 1L
            } finally {
                executorService.shutdownNow()
            }
        }

        it("닉네임이 50자를 초과하면 400") {
            signup(registrationKeyByLogin("google"), "가".repeat(51), true)
                .andExpect(status().isBadRequest)
        }

        it("registrationKey 형식이 UUID가 아니면 400") {
            signup("invalid-key", "길동", true)
                .andExpect(status().isBadRequest)
        }

        it("ID 토큰이 최대 길이를 초과하면 400") {
            login("google", "x".repeat(16_385))
                .andExpect(status().isBadRequest)
        }

        it("유저 소프트 삭제 시 deleted_at을 기록한다") {
            signup(registrationKeyByLogin("google"), "길동", true).andExpect(status().isCreated)
            val registeredUser = userRepository.findByProviderAndProviderId(OauthProvider.GOOGLE, "google-sub-1")
                .shouldNotBeNull()

            userRepository.delete(registeredUser)

            jdbcTemplate.queryForObject(
                "select deleted_at from users where id = ?",
                Timestamp::class.java,
                registeredUser.id,
            ).shouldNotBeNull()
        }

        it("탈퇴한 유저가 같은 소셜 계정으로 재가입하면 기존 행을 재활성화한다") {
            signup(registrationKeyByLogin("google"), "기존 닉네임", true).andExpect(status().isCreated)
            val deletedUser = userRepository
                .findByProviderAndProviderId(OauthProvider.GOOGLE, "google-sub-1")
                .shouldNotBeNull()
            userRepository.delete(deletedUser)

            signup(registrationKeyByLogin("google"), "  재가입 닉네임  ", true)
                .andExpect(status().isCreated)

            val reactivatedUser = userRepository
                .findByProviderAndProviderId(OauthProvider.GOOGLE, "google-sub-1")
                .shouldNotBeNull()
            reactivatedUser.id shouldBe deletedUser.id
            reactivatedUser.nickname shouldBe "재가입 닉네임"
            jdbcTemplate.queryForObject("select count(*) from users", Long::class.java) shouldBe 1L
            jdbcTemplate.queryForObject(
                "select deleted_at from users where id = ?",
                Timestamp::class.java,
                reactivatedUser.id,
            ) shouldBe null
        }

        it("카카오도 동일하게 동작한다 (미가입 202)") {
            login("kakao").andExpect(status().isAccepted)
        }

        it("지원하지 않는 프로바이더는 400(UOP)") {
            login("naver")
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.code").value("UOP"))
        }
    }

    describe("공개 엔드포인트") {
        it("Actuator health만 인증 없이 접근할 수 있다") {
            mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk)

            mockMvc.perform(get("/actuator"))
                .andExpect(status().isUnauthorized)
        }
    }
}) {

    @TestConfiguration
    class StubDecoderConfig {
        @Bean
        @Primary
        fun stubOidcDecoders(): Map<OauthProvider, JwtDecoder> = mapOf(
            OauthProvider.GOOGLE to
                JwtDecoder {
                    Jwt.withTokenValue("stub").header("alg", "RS256")
                        .subject("google-sub-1").claim("email", "g@example.com").build()
                },
            OauthProvider.KAKAO to
                JwtDecoder {
                    Jwt.withTokenValue("stub").header("alg", "RS256")
                        .subject("kakao-sub-1").claim("email", "k@example.com").build()
                },
        )
    }

    @TestConfiguration
    class InMemoryRegistrationStoreConfig {
        @Bean
        @Primary
        fun inMemoryRegistrationStore(): RegistrationStore = object : RegistrationStore {
            private val registrationInfoByKey = ConcurrentHashMap<String, RegistrationInfo>()

            override fun save(registrationInfo: RegistrationInfo): String =
                UUID.randomUUID().toString().also { registrationKey ->
                    registrationInfoByKey[registrationKey] = registrationInfo
                }

            override fun consume(registrationKey: String): RegistrationInfo? =
                registrationInfoByKey.remove(registrationKey)
        }
    }
}
