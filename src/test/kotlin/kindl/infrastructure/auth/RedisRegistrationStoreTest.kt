package kindl.infrastructure.auth

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kindl.domain.auth.model.RegistrationInfo
import kindl.domain.user.entity.OauthProvider
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.ValueOperations
import tools.jackson.databind.ObjectMapper
import java.time.Duration

class RedisRegistrationStoreTest : DescribeSpec({

    val redisTemplate = mockk<RedisTemplate<String, String>>()
    val redisValueOperations = mockk<ValueOperations<String, String>>()
    val objectMapper = mockk<ObjectMapper>()
    val redisRegistrationStore = RedisRegistrationStore(redisTemplate, objectMapper)
    val registrationInfo = RegistrationInfo(OauthProvider.GOOGLE, "sub-1", "user@kindl.test")

    beforeTest {
        every { redisTemplate.opsForValue() } returns redisValueOperations
    }

    describe("save") {
        it("가입 정보를 30분 TTL로 저장한다") {
            every { objectMapper.writeValueAsString(registrationInfo) } returns "json"
            every { redisValueOperations.set(any(), "json", Duration.ofMinutes(30)) } returns Unit

            val registrationKey = redisRegistrationStore.save(registrationInfo)

            verify(exactly = 1) {
                redisValueOperations.set("signup:$registrationKey", "json", Duration.ofMinutes(30))
            }
        }
    }

    describe("consume") {
        it("GETDEL로 가입 정보를 원자적으로 한 번만 소비한다") {
            every { redisValueOperations.getAndDelete("signup:key") } returns "json"
            every { objectMapper.readValue("json", RegistrationInfo::class.java) } returns registrationInfo

            redisRegistrationStore.consume("key") shouldBe registrationInfo

            verify(exactly = 1) { redisValueOperations.getAndDelete("signup:key") }
        }
    }
})
