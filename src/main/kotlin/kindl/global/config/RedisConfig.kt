package kindl.global.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.StringRedisSerializer

@Configuration
class RedisConfig {

    @Bean
    fun redisTemplate(connectionFactory: RedisConnectionFactory): RedisTemplate<String, String> {
        val stringRedisSerializer = StringRedisSerializer()
        val configuredRedisTemplate = RedisTemplate<String, String>()
        configuredRedisTemplate.connectionFactory = connectionFactory
        configuredRedisTemplate.keySerializer = stringRedisSerializer
        configuredRedisTemplate.valueSerializer = stringRedisSerializer
        configuredRedisTemplate.hashKeySerializer = stringRedisSerializer
        configuredRedisTemplate.hashValueSerializer = stringRedisSerializer
        return configuredRedisTemplate
    }
}
