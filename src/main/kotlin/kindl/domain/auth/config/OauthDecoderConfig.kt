package kindl.domain.auth.config

import kindl.domain.auth.service.OidcAudienceValidator
import kindl.domain.user.entity.OauthProvider
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
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

/**
 * 프로바이더별 OIDC ID 토큰 디코더를 구성한다.
 * 새 OIDC 프로바이더는 properties + 디코더 엔트리만 추가하면 된다(검증/로그인 로직은 provider-agnostic).
 */
@Configuration
@EnableConfigurationProperties(GoogleOauthProperties::class, KakaoOauthProperties::class)
class OauthDecoderConfig(
    private val googleOauthProperties: GoogleOauthProperties,
    private val kakaoOauthProperties: KakaoOauthProperties,
) {
    @Bean
    fun oidcDecoders(): Map<OauthProvider, JwtDecoder> = mapOf(
        OauthProvider.GOOGLE to googleDecoder(),
        OauthProvider.KAKAO to kakaoDecoder(),
    )

    private fun googleDecoder(): JwtDecoder = oidcDecoder(
        jwkSetUri = "https://www.googleapis.com/oauth2/v3/certs",
        issuers = setOf("https://accounts.google.com", "accounts.google.com"),
        audiences = googleOauthProperties.clientIds,
    )

    private fun kakaoDecoder(): JwtDecoder = oidcDecoder(
        jwkSetUri = "https://kauth.kakao.com/.well-known/jwks.json",
        issuers = setOf("https://kauth.kakao.com"),
        audiences = kakaoOauthProperties.clientIds,
    )

    private fun oidcDecoder(
        jwkSetUri: String,
        issuers: Set<String>,
        audiences: Collection<String>,
    ): JwtDecoder {
        val oidcDecoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build()
        oidcDecoder.setJwtValidator(
            DelegatingOAuth2TokenValidator(
                JwtValidators.createDefault(),
                issuerValidator(issuers),
                OidcAudienceValidator(audiences),
            ),
        )
        return oidcDecoder
    }

    private fun issuerValidator(issuers: Set<String>) = OAuth2TokenValidator<Jwt> { decodedJwt ->
        if (decodedJwt.getClaimAsString(JwtClaimNames.ISS) in issuers) {
            OAuth2TokenValidatorResult.success()
        } else {
            OAuth2TokenValidatorResult.failure(OAuth2Error(OAuth2ErrorCodes.INVALID_TOKEN, "Invalid issuer", null))
        }
    }

}
