package kindl.api.common.response

sealed interface ApiResponse {
    val status: Int
    val code: String
    val message: String
}
