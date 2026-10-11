package proov.api.common.response

data class SuccessResponse<T>(
    val status: Int,
    val code: String,
    val message: String,
    val data: T?,
)
