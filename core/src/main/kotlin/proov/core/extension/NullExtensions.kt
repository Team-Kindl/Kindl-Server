package proov.core.extension

import proov.core.code.FailureCode
import proov.core.exception.ProovException

fun <T : Any> T?.orThrow(failure: FailureCode): T = this ?: throw ProovException(failure)
