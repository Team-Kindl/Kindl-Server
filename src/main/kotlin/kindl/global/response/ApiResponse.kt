package kindl.global.response

sealed interface ApiResponse {
    val status: Int
    val code: String
    val message: String
}
