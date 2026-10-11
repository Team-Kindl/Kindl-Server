package proov.api.common.response

interface SuccessCode {
    val status: Int
    val code: String
    val message: String
}
