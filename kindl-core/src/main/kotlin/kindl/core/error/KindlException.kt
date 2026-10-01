package kindl.core.error

open class KindlException(
    val errorCode: ErrorCode,
    // 로그용 상세. 응답 본문에는 쓰지 않는다.
    detail: String? = null,
    cause: Throwable? = null,
) : RuntimeException(detail ?: errorCode.code, cause)
