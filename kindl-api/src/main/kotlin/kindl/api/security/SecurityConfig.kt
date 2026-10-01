package kindl.api.security

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator
import org.springframework.security.oauth2.jwt.JwtClaimValidator
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.JwtIssuerValidator
import org.springframework.security.web.SecurityFilterChain
import java.time.Clock

/**
 * access token 검증은 직접 필터를 짜지 않고 OAuth2 Resource Server에 맡긴다.
 * 401·403도 ErrorResponder를 거쳐 다른 오류와 같은 모양으로 나간다.
 */
@Configuration
@EnableConfigurationProperties(JwtProperties::class)
class SecurityConfig(
    private val entryPoint: TokenAuthenticationEntryPoint,
    private val accessDeniedHandler: TokenAccessDeniedHandler,
) {
    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http {
            httpBasic { disable() }
            formLogin { disable() }
            // 쿠키로 인증하지 않으므로 CSRF 공격면이 없다
            csrf { disable() }
            sessionManagement { sessionCreationPolicy = SessionCreationPolicy.STATELESS }
            authorizeHttpRequests {
                PUBLIC_PATHS.forEach { authorize(it, permitAll) }
                authorize(HttpMethod.POST, "/api/v1/auth/*/login", permitAll)
                authorize(HttpMethod.POST, "/api/v1/auth/refresh", permitAll)
                // signup token은 서비스가 따로 검증한다
                authorize(HttpMethod.POST, "/api/v1/users", permitAll)
                authorize(HttpMethod.GET, "/api/v1/nicknames/availability", permitAll)
                authorize(anyRequest, authenticated)
            }
            oauth2ResourceServer {
                jwt { }
                authenticationEntryPoint = entryPoint
            }
            exceptionHandling {
                authenticationEntryPoint = entryPoint
                accessDeniedHandler = this@SecurityConfig.accessDeniedHandler
            }
        }
        return http.build()
    }

    @Bean
    fun jwtDecoder(properties: JwtProperties, clock: Clock): JwtDecoder =
        JwtKeys.decoder(JwtKeys.secretKey(properties.accessKey)).apply {
            setJwtValidator(
                DelegatingOAuth2TokenValidator(
                    ExpiryValidator(clock, properties.clockSkew),
                    JwtIssuerValidator(properties.issuer),
                    JwtClaimValidator<String>(TokenClaims.TYPE) { it == TokenClaims.ACCESS },
                ),
            )
        }

    companion object {
        private val PUBLIC_PATHS = arrayOf(
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v3/api-docs/**",
            "/actuator/health",
            "/actuator/health/**",
        )
    }
}
