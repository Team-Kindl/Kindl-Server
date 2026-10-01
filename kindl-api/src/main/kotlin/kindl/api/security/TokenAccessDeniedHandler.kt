package kindl.api.security

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import kindl.api.common.exception.ErrorResponder
import kindl.core.error.CommonError
import org.springframework.http.MediaType
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.web.access.AccessDeniedHandler
import org.springframework.stereotype.Component
import tools.jackson.databind.json.JsonMapper

@Component
class TokenAccessDeniedHandler(
    private val responder: ErrorResponder,
    private val jsonMapper: JsonMapper,
) : AccessDeniedHandler {

    override fun handle(request: HttpServletRequest, response: HttpServletResponse, accessDeniedException: AccessDeniedException) {
        val entity = responder.respond(CommonError.FORBIDDEN_ACTION, request)
        response.status = entity.statusCode.value()
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.characterEncoding = Charsets.UTF_8.name()
        jsonMapper.writeValue(response.outputStream, entity.body)
    }
}
