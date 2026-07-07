package kindl.global.auth.security

import kindl.global.auth.jwt.dto.response.TokenResponse
import kindl.global.auth.jwt.JwtGenerator
import kindl.global.auth.jwt.JwtGenerator.Companion.ROLES_KEY
import kindl.global.auth.jwt.JwtGenerator.Companion.TYPE_KEY
import kindl.global.auth.jwt.JwtValidator
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
    fun issueToken(userId: String, userRoles: List<String> = emptyList()): TokenResponse {
        val accessToken = jwtGenerator.generateAccessToken(userId, userRoles)
        val refreshToken = jwtGenerator.generateRefreshToken(userId)
        return TokenResponse.of(accessToken, refreshToken)
    }

    fun getAuthentication(encodedToken: String): Authentication {
        val tokenClaims = jwtValidator.parse(encodedToken)

        if (tokenClaims[TYPE_KEY] != TokenType.ACCESS_TOKEN.name) {
            throw CustomException(ErrorCode.INVALID_TOKEN_TYPE)
        }
        val userId = tokenClaims.subject ?: throw CustomException(ErrorCode.INVALID_TOKEN)

        val userRoles = parseUserRoles(tokenClaims[ROLES_KEY])
        val grantedAuthorities = userRoles.map { userRole -> SimpleGrantedAuthority(ROLE_PREFIX + userRole) }

        return UsernamePasswordAuthenticationToken.authenticated(
            CustomUserDetails(userId, grantedAuthorities),
            null,
            grantedAuthorities,
        )
    }

    private fun parseUserRoles(rolesClaim: Any?): List<String> = when (rolesClaim) {
        null -> emptyList()
        is Collection<*> -> rolesClaim.map { userRole ->
            (userRole as? String)?.takeIf { roleName -> roleName.isNotBlank() }
                ?: throw CustomException(ErrorCode.INVALID_TOKEN)
        }
        else -> throw CustomException(ErrorCode.INVALID_TOKEN)
    }

    companion object {
        private const val ROLE_PREFIX = "ROLE_"
    }
}
