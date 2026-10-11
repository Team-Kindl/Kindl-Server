package proov.api.common.security

import org.springframework.security.oauth2.jose.jws.MacAlgorithm
import org.springframework.security.oauth2.jwt.JwsHeader
import org.springframework.security.oauth2.jwt.JwtClaimsSet
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.JwtEncoderParameters
import org.springframework.stereotype.Component
import proov.domain.auth.IssuedToken
import proov.domain.auth.port.AccessTokenIssuerPort
import java.time.Instant
import java.util.UUID

@Component
class AccessTokenIssuer(
    private val properties: JwtProperties,
) : AccessTokenIssuerPort {
    private val encoder: JwtEncoder = JwtKeys.encoder(JwtKeys.secretKey(properties.accessKey))

    override fun issue(userId: String, now: Instant): IssuedToken {
        val expiresAt = now.plus(properties.accessTtl)
        val claims = JwtClaimsSet.builder()
            .issuer(properties.issuer)
            .subject(userId)
            .id(UUID.randomUUID().toString())
            .issuedAt(now)
            .expiresAt(expiresAt)
            .claim(TokenClaims.TYPE, TokenClaims.ACCESS)
            .build()
        val token = encoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims))
        return IssuedToken(token.tokenValue, expiresAt)
    }
}
