package kindl.core.extension

import kindl.core.error.ErrorCode
import kindl.core.error.KindlException

fun <T : Any> T?.orThrow(errorCode: ErrorCode): T = this ?: throw KindlException(errorCode)
