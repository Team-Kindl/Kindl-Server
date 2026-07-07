package kindl.global.auth.oauth

import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import tools.jackson.databind.ObjectMapper
import java.time.Duration
import java.util.UUID

@Component
class RedisRegistrationStore(
    private val redisTemplate: RedisTemplate<String, String>,
    private val objectMapper: ObjectMapper,
) : RegistrationStore {

    override fun save(registrationInfo: RegistrationInfo): String {
        val registrationKey = UUID.randomUUID().toString()
        redisTemplate.opsForValue().set(
            redisKey(registrationKey),
            objectMapper.writeValueAsString(registrationInfo),
            REGISTRATION_TTL,
        )
        return registrationKey
    }

    override fun consume(registrationKey: String): RegistrationInfo? =
        redisTemplate.opsForValue().getAndDelete(redisKey(registrationKey))
            ?.let { serializedRegistrationInfo ->
                objectMapper.readValue(serializedRegistrationInfo, RegistrationInfo::class.java)
            }

    private fun redisKey(registrationKey: String) = "$KEY_PREFIX$registrationKey"

    companion object {
        private const val KEY_PREFIX = "signup:"
        private val REGISTRATION_TTL = Duration.ofMinutes(30)
    }
}
