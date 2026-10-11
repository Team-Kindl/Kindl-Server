package proov.domain.user.command

import proov.domain.user.DeviceSpec

data class DeviceCommand(
    val installationId: String,
    val spec: DeviceSpec,
)
