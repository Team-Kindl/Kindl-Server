package proov.core.exception

import proov.core.code.FailureCode

class ProovException(
    val failure: FailureCode,
    detail: String? = null,
    cause: Throwable? = null,
) : RuntimeException(detail ?: failure.code, cause)
