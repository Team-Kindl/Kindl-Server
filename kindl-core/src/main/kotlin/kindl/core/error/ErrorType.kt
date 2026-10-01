package kindl.core.error

/**
 * 오류의 성격. HTTP 상태로의 번역은 api 모듈 한 곳만 안다.
 */
enum class ErrorType {
    INVALID_INPUT,
    UNAUTHENTICATED,
    FORBIDDEN,
    NOT_FOUND,
    METHOD_NOT_ALLOWED,
    CONFLICT,
    RATE_LIMITED,
    EXTERNAL,
    INTERNAL,
}
