package proov.api.common.exception

import proov.core.code.FailureCode

enum class CommonFailure(
    override val status: Int,
    override val message: String,
) : FailureCode {
    INVALID_INPUT(400, "입력값을 확인해 주세요"),
    FORBIDDEN(403, "권한이 없어요"),
    NOT_FOUND(404, "찾을 수 없어요"),
    METHOD_NOT_ALLOWED(405, "지원하지 않는 요청 방식이에요"),
    INTERNAL_ERROR(500, "잠시 후 다시 시도해 주세요"),
    ;

    override val code: String get() = name
}
