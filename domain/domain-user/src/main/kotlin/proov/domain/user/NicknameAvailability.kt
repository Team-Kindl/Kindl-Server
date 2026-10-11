package proov.domain.user

import proov.core.code.FailureCode

data class NicknameAvailability(
    val available: Boolean,
    val reason: FailureCode?,
) {
    companion object {
        val AVAILABLE = NicknameAvailability(true, null)

        fun unavailable(reason: FailureCode) = NicknameAvailability(false, reason)
    }
}
