package proov.api.common.security

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.web.access.AccessDeniedHandler
import org.springframework.stereotype.Component
import proov.api.common.exception.CommonFailure

@Component
class TokenAccessDeniedHandler(
    private val jsonErrorWriter: JsonErrorWriter,
) : AccessDeniedHandler {

    override fun handle(request: HttpServletRequest, response: HttpServletResponse, accessDeniedException: AccessDeniedException) {
        jsonErrorWriter.write(response, CommonFailure.FORBIDDEN)
    }
}
