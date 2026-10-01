package kindl.api.security

import org.springframework.security.oauth2.jose.jws.MacAlgorithm
import org.springframework.security.oauth2.jwt.JwsHeader
import org.springframework.security.oauth2.jwt.JwtClaimsSet
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.JwtEncoderParameters
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.UUID

/** 30분짜리 access JWT. 닉네임처럼 바뀌는 값은 넣지 않는다. */
@Component
class AccessTokenIssuer(
    private val properties: JwtProperties,
) {
    private val encoder: JwtEncoder = JwtKeys.encoder(JwtKeys.secretKey(properties.accessKey))

    fun issue(userId: String, now: Instant): IssuedAccessToken {
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
        return IssuedAccessToken(token.tokenValue, expiresAt)
    }
}

data class IssuedAccessToken(
    val token: String,
    val expiresAt: Instant,
)
