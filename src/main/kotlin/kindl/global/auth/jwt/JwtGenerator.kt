package kindl.global.auth.jwt

import io.jsonwebtoken.Jwts
import org.springframework.stereotype.Component
import java.util.Date

@Component
class JwtGenerator(
    private val jwtProperties: JwtProperties,
    private val keyProvider: KeyProvider,
) {
    fun generateAccessToken(userId: String, roles: List<String>) =
        build(userId, TokenType.ACCESS_TOKEN, jwtProperties.accessTokenExpireTime, roles)

    fun generateRefreshToken(userId: String) =
        build(userId, TokenType.REFRESH_TOKEN, jwtProperties.refreshTokenExpireTime, null)

    private fun build(
        userId: String,
        type: TokenType,
        ttlMillis: Long,
        roles: List<String>?,
    ): TokenDto.Token {
        val now = System.currentTimeMillis()
        val expiredAt = now + ttlMillis
        val builder = Jwts.builder()
            .subject(userId)
            .claim(TYPE_KEY, type.name)
            .issuedAt(Date(now))
            .expiration(Date(expiredAt))
            .signWith(keyProvider.getSigningKey())
        if (roles != null) {
            builder.claim(ROLES_KEY, roles)
        }
        return TokenDto.Token(builder.compact(), expiredAt)
    }

    companion object {
        const val TYPE_KEY = "type"
        const val ROLES_KEY = "roles"
    }
}
