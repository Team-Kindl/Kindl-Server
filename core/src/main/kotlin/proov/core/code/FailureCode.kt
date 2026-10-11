package proov.core.code

interface FailureCode {
    val status: Int
    val code: String
    val message: String
}
