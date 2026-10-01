package kindl.domain.user.result

import kindl.domain.user.entity.Device

data class DeviceResult(
    val id: Long,
    val userId: String,
) {
    companion object {
        fun from(device: Device) = DeviceResult(id = requireNotNull(device.id), userId = device.userId)
    }
}
