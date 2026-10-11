package proov.api.controller.auth.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class RefreshRequest(
    @field:NotBlank @field:Size(max = 128)
    val refreshToken: String?,
)
