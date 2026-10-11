package proov.api.controller.auth.dto

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

data class LoginRequest(
    @field:NotBlank @field:Size(max = 16_384)
    val idToken: String?,
    @field:NotNull @field:Valid
    val device: DeviceRequest?,
)
