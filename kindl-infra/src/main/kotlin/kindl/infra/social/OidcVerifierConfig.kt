package kindl.infra.social

import kindl.core.type.SocialProvider
import kindl.domain.auth.port.SocialTokenVerifier
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator
import org.springframework.security.oauth2.core.OAuth2Error
import org.springframework.security.oauth2.core.OAuth2ErrorCodes
import org.springframework.security.oauth2.core.OAuth2TokenValidator
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtClaimNames
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.JwtValidators
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.web.client.RestTemplate

/** 테스트처럼 가짜 검증기를 쓰는 환경에서는 kindl.oauth.enabled=false로 끈다. */
@Configuration
@ConditionalOnProperty(prefix = "kindl.oauth", name = ["enabled"], havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(OauthProperties::class)
class OidcVerifierConfig(
    private val properties: OauthProperties,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Bean
    fun googleTokenVerifier(): SocialTokenVerifier = verifier(GOOGLE)

    @Bean
    fun kakaoTokenVerifier(): SocialTokenVerifier = verifier(KAKAO)

    @Bean
    fun appleTokenVerifier(): SocialTokenVerifier = verifier(APPLE)

    private fun verifier(spec: OidcSpec): SocialTokenVerifier {
        val clientIds = spec.clientIds(properties)
        if (clientIds.isEmpty()) {
            log.warn("{} client ID가 없어 이 제공자의 로그인을 받지 않습니다.", spec.provider)
            return OidcSocialTokenVerifier(spec.provider, decoder = null)
        }
        return OidcSocialTokenVerifier(spec.provider, decoder(spec, clientIds))
    }

    private fun decoder(spec: OidcSpec, clientIds: List<String>): JwtDecoder {
        // 외부 호출: 연결 1초, 응답 3초, 재시도 없음. 로그인은 사용자가 다시 누르면 된다
        val requestFactory = SimpleClientHttpRequestFactory().apply {
            setConnectTimeout(properties.connectTimeout)
            setReadTimeout(properties.readTimeout)
        }
        return NimbusJwtDecoder.withJwkSetUri(spec.jwkSetUri)
            .restOperations(RestTemplate(requestFactory))
            .build()
            .apply {
                setJwtValidator(
                    DelegatingOAuth2TokenValidator(
                        JwtValidators.createDefault(),
                        issuerValidator(spec.issuers),
                        OidcAudienceValidator(clientIds),
                    ),
                )
            }
    }

    private fun issuerValidator(issuers: Set<String>) = OAuth2TokenValidator<Jwt> { jwt ->
        if (jwt.getClaimAsString(JwtClaimNames.ISS) in issuers) {
            OAuth2TokenValidatorResult.success()
        } else {
            OAuth2TokenValidatorResult.failure(OAuth2Error(OAuth2ErrorCodes.INVALID_TOKEN, "Invalid issuer", null))
        }
    }

    private data class OidcSpec(
        val provider: SocialProvider,
        val jwkSetUri: String,
        val issuers: Set<String>,
        val clientIds: (OauthProperties) -> List<String>,
    )

    companion object {
        private val GOOGLE = OidcSpec(
            provider = SocialProvider.GOOGLE,
            jwkSetUri = "https://www.googleapis.com/oauth2/v3/certs",
            issuers = setOf("https://accounts.google.com", "accounts.google.com"),
            clientIds = { it.google.enabledClientIds },
        )
        private val KAKAO = OidcSpec(
            provider = SocialProvider.KAKAO,
            jwkSetUri = "https://kauth.kakao.com/.well-known/jwks.json",
            issuers = setOf("https://kauth.kakao.com"),
            clientIds = { it.kakao.enabledClientIds },
        )
        private val APPLE = OidcSpec(
            provider = SocialProvider.APPLE,
            jwkSetUri = "https://appleid.apple.com/auth/keys",
            issuers = setOf("https://appleid.apple.com"),
            clientIds = { it.apple.enabledClientIds },
        )
    }
}
