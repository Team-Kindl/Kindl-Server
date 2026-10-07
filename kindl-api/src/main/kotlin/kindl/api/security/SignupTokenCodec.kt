package kindl.api.security

import kindl.core.error.KindlException
import kindl.core.type.SocialProvider
import kindl.domain.auth.error.AuthError
import kindl.domain.auth.port.SocialIdentity
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator
import org.springframework.security.oauth2.jose.jws.MacAlgorithm
import org.springframework.security.oauth2.jwt.JwsHeader
import org.springframework.security.oauth2.jwt.JwtClaimValidator
import org.springframework.security.oauth2.jwt.JwtClaimsSet
import org.springframework.security.oauth2.jwt.JwtEncoderParameters
import org.springframework.security.oauth2.jwt.JwtException
import org.springframework.security.oauth2.jwt.JwtIssuerValidator
import org.springframework.stereotype.Component
import java.time.Clock
import java.time.Instant

/**
 * 소셜 로그인만 끝낸 사람에게 주는 10분짜리 가입 토큰. DB에 저장하지 않는다.
 * 유저는 이 토큰으로 프로필을 저장하는 순간 만들어진다 → 가입 중간에 앱을 꺼도 빈 유저가 남지 않는다.
 */
@Component
class SignupTokenCodec(
    private val properties: JwtProperties,
    clock: Clock,
) {
    private val key = JwtKeys.secretKey(properties.signupKey)
    private val encoder = JwtKeys.encoder(key)
    private val decoder = JwtKeys.decoder(key).apply {
        setJwtValidator(
            DelegatingOAuth2TokenValidator(
                ExpiryValidator(clock, properties.clockSkew),
                JwtIssuerValidator(properties.issuer),
                JwtClaimValidator<String>(TokenClaims.TYPE) { it == TokenClaims.SIGNUP },
            ),
        )
    }

    fun issue(identity: SocialIdentity, now: Instant): String {
        val claims = JwtClaimsSet.builder()
            .issuer(properties.issuer)
            .subject(identity.providerUserId)
            .issuedAt(now)
            .expiresAt(now.plus(properties.signupTtl))
            .claim(TokenClaims.TYPE, TokenClaims.SIGNUP)
            .claim(TokenClaims.PROVIDER, identity.provider.name)
            .claim(TokenClaims.PROVIDER_USER_ID, identity.providerUserId)
            .apply { identity.email?.let { claim(TokenClaims.EMAIL, it) } }
            .build()
        return encoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims)).tokenValue
    }

    fun decode(token: String): SocialIdentity {
        val jwt = try {
            decoder.decode(token)
        } catch (e: JwtException) {
            throw KindlException(AuthError.SIGNUP_TOKEN_INVALID, e.message, e)
        }
        val provider = jwt.getClaimAsString(TokenClaims.PROVIDER)
            ?.let { runCatching { SocialProvider.valueOf(it) }.getOrNull() }
        val providerUserId = jwt.getClaimAsString(TokenClaims.PROVIDER_USER_ID)
        if (provider == null || providerUserId.isNullOrBlank()) throw KindlException(AuthError.SIGNUP_TOKEN_INVALID)
        return SocialIdentity(provider, providerUserId, jwt.getClaimAsString(TokenClaims.EMAIL))
    }
}
