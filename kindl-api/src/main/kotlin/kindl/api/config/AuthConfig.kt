package kindl.api.config

import kindl.api.security.JwtProperties
import kindl.domain.auth.service.RefreshTokenPolicy
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class AuthConfig {

    @Bean
    fun refreshTokenPolicy(properties: JwtProperties) =
        RefreshTokenPolicy(ttl = properties.refreshTtl, reuseGrace = properties.refreshReuseGrace)
}
