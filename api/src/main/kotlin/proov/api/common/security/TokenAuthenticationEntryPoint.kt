package proov.api.common.security

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders
import org.springframework.security.core.AuthenticationException
import org.springframework.security.oauth2.jwt.JwtValidationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.stereotype.Component
import proov.domain.auth.exception.AuthFailure

@Component
class TokenAuthenticationEntryPoint(
    private val jsonErrorWriter: JsonErrorWriter,
) : AuthenticationEntryPoint {

    override fun commence(request: HttpServletRequest, response: HttpServletResponse, authException: AuthenticationException) {
        val failure = when {
            request.getHeader(HttpHeaders.AUTHORIZATION).isNullOrBlank() -> AuthFailure.AUTH_REQUIRED
            authException.isExpired() -> AuthFailure.TOKEN_EXPIRED
            else -> AuthFailure.TOKEN_INVALID
        }
        jsonErrorWriter.write(response, failure)
    }

    private fun Throwable.isExpired(): Boolean =
        generateSequence(this) { it.cause }
            .filterIsInstance<JwtValidationException>()
            .any { e -> e.errors.any { it.errorCode == TokenClaims.EXPIRED_ERROR_CODE } }
}
