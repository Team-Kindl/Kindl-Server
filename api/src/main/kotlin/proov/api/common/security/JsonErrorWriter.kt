package proov.api.common.security

import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import proov.api.common.response.ResponseFactory
import proov.core.code.FailureCode
import tools.jackson.databind.json.JsonMapper

@Component
class JsonErrorWriter(
    private val jsonMapper: JsonMapper,
) {
    fun write(response: HttpServletResponse, failure: FailureCode) {
        response.status = failure.status
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.characterEncoding = Charsets.UTF_8.name()
        jsonMapper.writeValue(response.outputStream, ResponseFactory.failure(failure).body)
    }
}
