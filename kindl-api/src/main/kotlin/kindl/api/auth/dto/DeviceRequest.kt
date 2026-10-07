package kindl.api.auth.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import kindl.core.type.Platform
import kindl.domain.user.command.DeviceCommand
import kindl.domain.user.entity.DeviceSpec

/**
 * 필수 필드를 nullable로 받는다: non-null이면 필드가 빠진 JSON에서 검증보다 역직렬화 예외가 먼저 나
 * 어떤 필드가 빠졌는지 담긴 400을 만들기 어렵다.
 */
data class DeviceRequest(
    @field:NotBlank @field:Size(max = 64)
    val installationId: String?,
    @field:NotNull
    val platform: Platform?,
    @field:NotBlank @field:Size(max = 20)
    val osVersion: String?,
    @field:NotBlank @field:Size(max = 20)
    val appVersion: String?,
) {
    fun toCommand() = DeviceCommand(
        installationId = installationId!!,
        spec = DeviceSpec(platform!!, osVersion!!, appVersion!!),
    )
}
