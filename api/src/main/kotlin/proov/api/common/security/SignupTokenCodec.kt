package proov.api.common.security

import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator
import org.springframework.security.oauth2.jose.jws.MacAlgorithm
import org.springframework.security.oauth2.jwt.JwsHeader
import org.springframework.security.oauth2.jwt.JwtClaimValidator
import org.springframework.security.oauth2.jwt.JwtClaimsSet
import org.springframework.security.oauth2.jwt.JwtEncoderParameters
import org.springframework.security.oauth2.jwt.JwtException
import org.springframework.security.oauth2.jwt.JwtIssuerValidator
import org.springframework.stereotype.Component
import proov.core.exception.ProovException
import proov.core.extension.toEnumIgnoreCaseOrNull
import proov.core.type.SocialProvider
import proov.domain.auth.SocialIdentity
import proov.domain.auth.exception.AuthFailure
import proov.domain.auth.port.SignupTokenPort
import java.time.Clock
import java.time.Instant

@Component
class SignupTokenCodec(
    private val properties: JwtProperties,
    clock: Clock,
) : SignupTokenPort {
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

    override fun issue(identity: SocialIdentity, now: Instant): String {
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

    override fun decode(token: String): SocialIdentity {
        val jwt = try {
            decoder.decode(token)
        } catch (e: JwtException) {
            throw ProovException(AuthFailure.SIGNUP_TOKEN_INVALID, e.message, e)
        }
        val provider = jwt.getClaimAsString(TokenClaims.PROVIDER)?.toEnumIgnoreCaseOrNull<SocialProvider>()
        val providerUserId = jwt.getClaimAsString(TokenClaims.PROVIDER_USER_ID)
        if (provider == null || providerUserId.isNullOrBlank()) throw ProovException(AuthFailure.SIGNUP_TOKEN_INVALID)
        return SocialIdentity(provider, providerUserId, jwt.getClaimAsString(TokenClaims.EMAIL))
    }
}
