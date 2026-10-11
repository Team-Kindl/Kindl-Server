package proov.api.common.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import proov.api.common.security.JwtProperties
import proov.domain.auth.RefreshTokenPolicy

@Configuration
class AuthConfig {

    @Bean
    fun refreshTokenPolicy(properties: JwtProperties) =
        RefreshTokenPolicy(ttl = properties.refreshTtl, reuseGrace = properties.refreshReuseGrace)
}
