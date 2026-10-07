package kindl.domain.user.dto.result

import kindl.core.error.ErrorCode

/** 불가능하면 이유 코드를 함께 준다 (NICKNAME_LENGTH, NICKNAME_TAKEN …) */
data class NicknameAvailability(
    val available: Boolean,
    val reason: ErrorCode?,
) {
    companion object {
        val AVAILABLE = NicknameAvailability(true, null)

        fun unavailable(reason: ErrorCode) = NicknameAvailability(false, reason)
    }
}
