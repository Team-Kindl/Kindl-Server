package kindl.global.response

import kindl.global.exception.ErrorCode
import org.springframework.http.ResponseEntity

@ConsistentCopyVisibility
data class FailureResponse private constructor(
    override val status: Int,
    override val code: String,
    override val message: String,
) : ApiResponse {
    companion object {
        fun of(
            errorCode: ErrorCode,
        ): ResponseEntity<FailureResponse> = of(errorCode, errorCode.message)

        fun of(
            errorCode: ErrorCode,
            message: String,
        ): ResponseEntity<FailureResponse> = ResponseEntity
            .status(errorCode.httpStatus)
            .body(
                FailureResponse(
                    status = errorCode.httpStatus.value(),
                    code = errorCode.code,
                    message = message,
                ),
            )
    }
}
