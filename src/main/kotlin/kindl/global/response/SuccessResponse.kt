package kindl.global.response

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

@ConsistentCopyVisibility
data class SuccessResponse<T> private constructor(
    override val status: Int,
    override val code: String,
    override val message: String,
    val data: T?,
) : ApiResponse {
    companion object {
        fun <T> of(
            data: T?,
            status: HttpStatus = HttpStatus.OK,
            code: String = "OK",
            message: String = "요청에 성공했습니다.",
        ): ResponseEntity<SuccessResponse<T>> = ResponseEntity
            .status(status)
            .body(SuccessResponse(status.value(), code, message, data))
    }
}
