package kindl.api.user.dto.response

import com.fasterxml.jackson.annotation.JsonInclude
import kindl.domain.user.dto.result.NicknameAvailability

@JsonInclude(JsonInclude.Include.NON_NULL)
data class NicknameAvailabilityResponse(
    val available: Boolean,
    // NICKNAME_LENGTH · NICKNAME_INVALID_CHAR · NICKNAME_BLOCKED · NICKNAME_TAKEN
    val reason: String?,
) {
    companion object {
        fun from(result: NicknameAvailability) = NicknameAvailabilityResponse(result.available, result.reason?.code)
    }
}
