package proov.api.controller.user.dto

import com.fasterxml.jackson.annotation.JsonInclude
import proov.domain.user.NicknameAvailability

@JsonInclude(JsonInclude.Include.NON_NULL)
data class NicknameAvailabilityResponse(
    val available: Boolean,
    val reason: String?,
) {
    companion object {
        fun from(result: NicknameAvailability) = NicknameAvailabilityResponse(result.available, result.reason?.code)
    }
}
