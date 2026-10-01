package kindl.core.error

enum class CommonError(override val type: ErrorType) : ErrorCode {
    INVALID_INPUT(ErrorType.INVALID_INPUT),
    RESOURCE_NOT_FOUND(ErrorType.NOT_FOUND),
    FORBIDDEN_ACTION(ErrorType.FORBIDDEN),
    METHOD_NOT_ALLOWED(ErrorType.METHOD_NOT_ALLOWED),
    CONFLICT_STATE(ErrorType.CONFLICT),
    INTERNAL_ERROR(ErrorType.INTERNAL),
    ;

    override val code: String get() = name
}
