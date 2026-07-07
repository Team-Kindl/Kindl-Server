package kindl.global.auth.jwt

import io.jsonwebtoken.Jwts
import kindl.global.auth.jwt.dto.response.TokenResponse
import org.springframework.stereotype.Component
import java.util.Date

@Component
class JwtGenerator(
    private val jwtProperties: JwtProperties,
    private val keyProvider: KeyProvider,
) {
    fun generateAccessToken(userId: String, userRoles: List<String>): TokenResponse.TokenDetailResponse =
        build(userId, TokenType.ACCESS_TOKEN, jwtProperties.accessTokenExpireTime, userRoles)

    fun generateRefreshToken(userId: String): TokenResponse.TokenDetailResponse =
        build(userId, TokenType.REFRESH_TOKEN, jwtProperties.refreshTokenExpireTime, null)

    private fun build(
        userId: String,
        tokenType: TokenType,
        timeToLiveMillis: Long,
        userRoles: List<String>?,
    ): TokenResponse.TokenDetailResponse {
        val issuedAtMillis = System.currentTimeMillis()
        val expirationAtMillis = issuedAtMillis + timeToLiveMillis
        val jwtBuilder = Jwts.builder()
            .subject(userId)
            .claim(TYPE_KEY, tokenType.name)
            .issuedAt(Date(issuedAtMillis))
            .expiration(Date(expirationAtMillis))
            .signWith(keyProvider.getSigningKey())
        if (userRoles != null) {
            jwtBuilder.claim(ROLES_KEY, userRoles)
        }
        return TokenResponse.TokenDetailResponse.of(
            token = jwtBuilder.compact(),
            expiredAt = expirationAtMillis,
        )
    }

    companion object {
        const val TYPE_KEY = "type"
        const val ROLES_KEY = "roles"
    }
}
