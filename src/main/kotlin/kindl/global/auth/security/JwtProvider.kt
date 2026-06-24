package kindl.global.auth.security

import kindl.global.auth.jwt.JwtGenerator
import kindl.global.auth.jwt.JwtGenerator.Companion.ROLES_KEY
import kindl.global.auth.jwt.JwtGenerator.Companion.TYPE_KEY
import kindl.global.auth.jwt.JwtValidator
import kindl.global.auth.jwt.TokenDto
import kindl.global.auth.jwt.TokenType
import kindl.global.exception.CustomException
import kindl.global.exception.ErrorCode
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.stereotype.Component

@Component
class JwtProvider(
    private val jwtGenerator: JwtGenerator,
    private val jwtValidator: JwtValidator,
) {
    fun issueToken(userId: String, roles: List<String> = emptyList()): TokenDto {
        val access = jwtGenerator.generateAccessToken(userId, roles)
        val refresh = jwtGenerator.generateRefreshToken(userId)
        return TokenDto.of(
            accessToken = access.token,
            accessTokenExpiredAt = access.expiredAt,
            refreshToken = refresh.token,
            refreshTokenExpiredAt = refresh.expiredAt,
        )
    }

    fun getAuthentication(token: String): Authentication {
        val claims = jwtValidator.parse(token)

        if (claims[TYPE_KEY] != TokenType.ACCESS_TOKEN.name) {
            throw CustomException(ErrorCode.INVALID_TOKEN_TYPE)
        }
        val subject = claims.subject ?: throw CustomException(ErrorCode.INVALID_TOKEN)

        val roles = claims[ROLES_KEY] as? List<*> ?: emptyList<Any?>()
        val authorities = roles.map { SimpleGrantedAuthority(ROLE_PREFIX + it) }

        return UsernamePasswordAuthenticationToken(
            CustomUserDetails(subject, authorities),
            null,
            authorities,
        )
    }

    companion object {
        private const val ROLE_PREFIX = "ROLE_"
    }
}
