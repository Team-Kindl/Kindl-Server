package proov.api.controller.auth.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import proov.api.common.validation.ValidEnum
import proov.core.extension.toEnumIgnoreCase
import proov.core.type.Platform
import proov.domain.user.DeviceSpec
import proov.domain.user.command.DeviceCommand

data class DeviceRequest(
    @field:NotBlank @field:Size(max = 64)
    val installationId: String?,
    @field:NotBlank @field:ValidEnum(Platform::class)
    val platform: String?,
    @field:NotBlank @field:Size(max = 20)
    val osVersion: String?,
    @field:NotBlank @field:Size(max = 20)
    val appVersion: String?,
) {
    fun toCommand() = DeviceCommand(
        installationId = installationId!!,
        spec = DeviceSpec(platform!!.toEnumIgnoreCase(), osVersion!!, appVersion!!),
    )
}
