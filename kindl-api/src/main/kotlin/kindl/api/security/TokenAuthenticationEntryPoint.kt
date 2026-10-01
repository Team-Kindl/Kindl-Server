package kindl.api.security

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import kindl.api.common.exception.ErrorResponder
import kindl.domain.auth.error.AuthError
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.security.core.AuthenticationException
import org.springframework.security.oauth2.jwt.JwtValidationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.stereotype.Component
import tools.jackson.databind.json.JsonMapper

/**
 * 401을 앱이 다음 행동을 고를 수 있는 코드로 나눈다.
 * 헤더 없음 → AUTH_REQUIRED, 만료 → TOKEN_EXPIRED(refresh 후 재시도), 그 외 → TOKEN_INVALID(로그인).
 */
@Component
class TokenAuthenticationEntryPoint(
    private val responder: ErrorResponder,
    private val jsonMapper: JsonMapper,
) : AuthenticationEntryPoint {

    override fun commence(request: HttpServletRequest, response: HttpServletResponse, authException: AuthenticationException) {
        val errorCode = when {
            request.getHeader(HttpHeaders.AUTHORIZATION).isNullOrBlank() -> AuthError.AUTH_REQUIRED
            authException.isExpired() -> AuthError.TOKEN_EXPIRED
            else -> AuthError.TOKEN_INVALID
        }
        val entity = responder.respond(errorCode, request)
        response.status = entity.statusCode.value()
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.characterEncoding = Charsets.UTF_8.name()
        jsonMapper.writeValue(response.outputStream, entity.body)
    }

    private fun Throwable.isExpired(): Boolean =
        generateSequence(this) { it.cause }
            .filterIsInstance<JwtValidationException>()
            .any { e -> e.errors.any { it.errorCode == TokenClaims.EXPIRED_ERROR_CODE } }
}
