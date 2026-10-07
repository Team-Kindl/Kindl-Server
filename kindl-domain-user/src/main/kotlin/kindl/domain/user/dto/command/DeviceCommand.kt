package kindl.domain.user.dto.command

import kindl.domain.user.entity.DeviceSpec

data class DeviceCommand(
    val installationId: String,
    val spec: DeviceSpec,
)
