package proov.api.common.response

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class FailureResponse(
    val status: Int,
    val code: String,
    val message: String,
    val errors: List<FieldError>? = null,
) {
    data class FieldError(
        val field: String,
        val reason: String,
    )
}
