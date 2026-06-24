package kindl.global.auth.security

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import kindl.global.exception.CustomException
import kindl.global.exception.ErrorCode
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerExceptionResolver

@Component
class JwtAuthenticationEntryPoint(
    @param:Qualifier("handlerExceptionResolver")
    private val resolver: HandlerExceptionResolver,
) : AuthenticationEntryPoint {

    override fun commence(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authException: AuthenticationException,
    ) {
        val exception = request.getAttribute(EXCEPTION_KEY) as? Exception
            ?: CustomException(ErrorCode.UNAUTHORIZED)
        resolver.resolveException(request, response, null, exception)
    }

    companion object {
        const val EXCEPTION_KEY = "exception"
    }
}
