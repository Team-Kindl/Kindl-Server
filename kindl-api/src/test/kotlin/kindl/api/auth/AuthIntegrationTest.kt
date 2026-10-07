package kindl.api.auth

import com.jayway.jsonpath.JsonPath
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldHaveLength
import kindl.domain.auth.fixture.FakeSocialTokenVerifier
import kindl.support.IntegrationTestConfig
import kindl.support.MutableClock
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.Duration
import java.time.Instant

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(IntegrationTestConfig::class)
class AuthIntegrationTest(
    private val mockMvc: MockMvc,
    private val jdbcTemplate: JdbcTemplate,
    private val clock: MutableClock,
) : DescribeSpec({

    val device = """
        {"installationId":"install-1","platform":"IOS","osVersion":"18.0","appVersion":"1.0.0"}
    """.trimIndent()

    fun login(socialToken: String, provider: String = "kakao"): ResultActions = mockMvc.perform(
        post("/api/v1/auth/$provider/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"idToken":"$socialToken","device":$device}"""),
    )

    fun signup(signupToken: String, nickname: String, language: String = "ko"): ResultActions = mockMvc.perform(
        post("/api/v1/users")
            .header(HttpHeaders.ACCEPT_LANGUAGE, language)
            .contentType(MediaType.APPLICATION_JSON)
            .content(
                """{"signupToken":"$signupToken","nickname":"$nickname","timezone":"Asia/Seoul",
                   "locale":"ko-KR","termsVersion":"1.0","device":$device}""",
            ),
    )

    fun refresh(refreshToken: String): ResultActions = mockMvc.perform(
        post("/api/v1/auth/refresh")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"refreshToken":"$refreshToken"}"""),
    )

    fun me(accessToken: String?): ResultActions = mockMvc.perform(
        get("/api/v1/users/me").apply { accessToken?.let { header(HttpHeaders.AUTHORIZATION, "Bearer $it") } },
    )

    fun ResultActions.read(path: String): String = JsonPath.read(andReturn().response.contentAsString, path)

    fun signupTokenOf(socialToken: String): String = login(socialToken).read("$.data.signupToken")

    /** 가입까지 마치고 (accessToken, refreshToken)을 돌려준다. */
    fun signedUp(socialToken: String, nickname: String): Pair<String, String> {
        val response = signup(signupTokenOf(socialToken), nickname).andExpect(status().isCreated)
        return response.read("$.data.accessToken") to response.read("$.data.refreshToken")
    }

    beforeTest {
        clock.set(IntegrationTestConfig.START)
        listOf("refresh_tokens", "devices", "social_accounts", "users").forEach { jdbcTemplate.execute("delete from $it") }
    }

    describe("소셜 로그인과 가입") {
        it("처음 보는 소셜 계정이면 유저를 만들지 않고 signupToken만 준다") {
            login("kakao-1")
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.data.status").value("SIGNUP_REQUIRED"))
                .andExpect(jsonPath("$.data.tokens").doesNotExist())

            jdbcTemplate.queryForObject("select count(*) from users", Int::class.java) shouldBe 0
        }

        it("익명 프로필을 저장하면 유저·소셜 계정·기기를 한 번에 만들고 토큰을 준다") {
            val (accessToken, _) = signedUp("kakao-1", "성실한 수달")

            val userId = me(accessToken)
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.data.nickname").value("성실한 수달"))
                .andExpect(jsonPath("$.data.timezone").value("Asia/Seoul"))
                .read("$.data.id")
            userId shouldHaveLength 13
            jdbcTemplate.queryForObject("select count(*) from social_accounts where user_id = ?", Int::class.java, userId) shouldBe 1
            jdbcTemplate.queryForObject("select count(*) from devices where user_id = ?", Int::class.java, userId) shouldBe 1
        }

        it("가입한 뒤 다시 로그인하면 SIGNED_IN과 토큰을 준다") {
            signedUp("kakao-1", "성실한 수달")

            login("kakao-1")
                .andExpect(jsonPath("$.data.status").value("SIGNED_IN"))
                .andExpect(jsonPath("$.data.tokens.accessToken").isString)
        }

        it("같은 사람이라도 다른 제공자로 로그인하면 다른 계정이다") {
            signedUp("same-sub", "성실한 수달")

            login("same-sub", provider = "google").andExpect(jsonPath("$.data.status").value("SIGNUP_REQUIRED"))
        }

        it("가입 버튼을 두 번 누르면 두 번째는 409 ALREADY_SIGNED_UP") {
            val signupToken = signupTokenOf("kakao-1")
            signup(signupToken, "성실한 수달").andExpect(status().isCreated)

            signup(signupToken, "다른 수달")
                .andExpect(status().isConflict)
                .andExpect(jsonPath("$.code").value("ALREADY_SIGNED_UP"))
        }

        it("이미 쓰는 닉네임은 409 NICKNAME_TAKEN, 문구는 Accept-Language를 따른다") {
            signedUp("kakao-1", "Otter")

            signup(signupTokenOf("kakao-2"), "ＯＴＴＥＲ", language = "ko")
                .andExpect(status().isConflict)
                .andExpect(jsonPath("$.code").value("NICKNAME_TAKEN"))
                .andExpect(jsonPath("$.message").value("이미 사용 중인 닉네임이에요"))
            signup(signupTokenOf("kakao-2"), "otter", language = "en-US")
                .andExpect(jsonPath("$.message").value("This nickname is already taken"))
        }

        it("탈퇴(soft delete)한 유저의 닉네임은 다시 쓸 수 있다") {
            signedUp("kakao-1", "성실한 수달")
            jdbcTemplate.update("update users set deleted_at = ?", java.sql.Timestamp.from(clock.instant()))

            signup(signupTokenOf("kakao-2"), "성실한 수달").andExpect(status().isCreated)
        }

        it("이모지 닉네임은 400 NICKNAME_INVALID_CHAR") {
            signup(signupTokenOf("kakao-1"), "수달😀")
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.code").value("NICKNAME_INVALID_CHAR"))
        }

        it("10분이 지난 signupToken은 401 SIGNUP_TOKEN_INVALID") {
            val signupToken = signupTokenOf("kakao-1")
            clock.advance(Duration.ofMinutes(11))

            signup(signupToken, "성실한 수달")
                .andExpect(status().isUnauthorized)
                .andExpect(jsonPath("$.code").value("SIGNUP_TOKEN_INVALID"))
        }

        it("위조된 소셜 토큰은 401, 제공자 장애는 503") {
            login(FakeSocialTokenVerifier.INVALID).andExpect(status().isUnauthorized)
                .andExpect(jsonPath("$.code").value("SOCIAL_TOKEN_INVALID"))
            login(FakeSocialTokenVerifier.UNAVAILABLE).andExpect(status().isServiceUnavailable)
                .andExpect(jsonPath("$.code").value("SOCIAL_PROVIDER_UNAVAILABLE"))
        }

        it("모르는 제공자는 400 SOCIAL_PROVIDER_UNSUPPORTED") {
            login("t", provider = "naver").andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.code").value("SOCIAL_PROVIDER_UNSUPPORTED"))
        }

        it("필수 필드가 빠지면 필드별 원인을 한 번에 돌려준다") {
            mockMvc.perform(post("/api/v1/auth/kakao/login").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"))
                .andExpect(jsonPath("$.errors.length()").value(2))
        }
    }

    describe("access token") {
        it("헤더가 없으면 401 AUTH_REQUIRED") {
            me(null).andExpect(status().isUnauthorized).andExpect(jsonPath("$.code").value("AUTH_REQUIRED"))
        }

        it("30분이 지나면 401 TOKEN_EXPIRED") {
            val (accessToken, _) = signedUp("kakao-1", "성실한 수달")
            clock.advance(Duration.ofMinutes(31))

            me(accessToken).andExpect(status().isUnauthorized).andExpect(jsonPath("$.code").value("TOKEN_EXPIRED"))
        }

        it("signupToken을 access 자리에 넣으면 401 TOKEN_INVALID") {
            me(signupTokenOf("kakao-1")).andExpect(status().isUnauthorized).andExpect(jsonPath("$.code").value("TOKEN_INVALID"))
        }

        it("탈퇴한 유저의 토큰은 만료 전이어도 401 USER_NOT_FOUND") {
            val (accessToken, _) = signedUp("kakao-1", "성실한 수달")
            jdbcTemplate.update("update users set deleted_at = ?", java.sql.Timestamp.from(clock.instant()))

            me(accessToken).andExpect(status().isUnauthorized).andExpect(jsonPath("$.code").value("USER_NOT_FOUND"))
        }
    }

    describe("refresh token 회전") {
        it("쓸 때마다 새 refresh token을 준다") {
            val (_, refreshToken) = signedUp("kakao-1", "성실한 수달")

            val rotated = refresh(refreshToken).andExpect(status().isOk).read("$.data.refreshToken")

            (rotated != refreshToken) shouldBe true
            refresh(rotated).andExpect(status().isOk)
        }

        it("회전 직후 10초 안의 옛 토큰은 409 REFRESH_RACE, 새 토큰은 그대로 쓸 수 있다") {
            val (_, refreshToken) = signedUp("kakao-1", "성실한 수달")
            val rotated = refresh(refreshToken).read("$.data.refreshToken")
            clock.advance(Duration.ofSeconds(5))

            refresh(refreshToken).andExpect(status().isConflict).andExpect(jsonPath("$.code").value("REFRESH_RACE"))
            refresh(rotated).andExpect(status().isOk)
        }

        it("유예가 지난 옛 토큰을 다시 쓰면 401 REFRESH_REUSED, 그 묶음 전체가 폐기된다") {
            val (_, refreshToken) = signedUp("kakao-1", "성실한 수달")
            val rotated = refresh(refreshToken).read("$.data.refreshToken")
            clock.advance(Duration.ofSeconds(11))

            refresh(refreshToken).andExpect(status().isUnauthorized).andExpect(jsonPath("$.code").value("REFRESH_REUSED"))
            refresh(rotated).andExpect(status().isUnauthorized).andExpect(jsonPath("$.code").value("REFRESH_INVALID"))
        }

        it("30일이 지난 refresh token은 401 REFRESH_INVALID") {
            val (_, refreshToken) = signedUp("kakao-1", "성실한 수달")
            clock.advance(Duration.ofDays(30))

            refresh(refreshToken).andExpect(status().isUnauthorized).andExpect(jsonPath("$.code").value("REFRESH_INVALID"))
        }

        it("로그아웃하면 그 기기의 refresh token을 더 쓸 수 없다") {
            val (accessToken, refreshToken) = signedUp("kakao-1", "성실한 수달")

            mockMvc.perform(
                post("/api/v1/auth/logout")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer $accessToken")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"refreshToken":"$refreshToken"}"""),
            ).andExpect(status().isOk)

            refresh(refreshToken).andExpect(status().isUnauthorized).andExpect(jsonPath("$.code").value("REFRESH_INVALID"))
        }
    }

    describe("저장 형식") {
        it("시각은 UTC로 저장된다") {
            signedUp("kakao-1", "성실한 수달")

            val stored = jdbcTemplate.queryForObject("select cast(terms_agreed_at as char) from users", String::class.java)
            stored shouldBe "2026-10-01 00:00:00.000000"
            Instant.parse("2026-10-01T00:00:00Z") shouldBe IntegrationTestConfig.START
        }
    }
})
