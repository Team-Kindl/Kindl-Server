package proov.api.common.response

import org.springframework.http.ResponseEntity
import proov.core.code.FailureCode

object ResponseFactory {

    fun <T> success(code: SuccessCode, data: T? = null): ResponseEntity<SuccessResponse<T>> =
        ResponseEntity.status(code.status).body(SuccessResponse(code.status, code.code, code.message, data))

    fun failure(failure: FailureCode, errors: List<FailureResponse.FieldError>? = null): ResponseEntity<FailureResponse> =
        ResponseEntity.status(failure.status).body(FailureResponse(failure.status, failure.code, failure.message, errors))
}
