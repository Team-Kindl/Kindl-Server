package proov.domain.user.port

import proov.domain.user.Device

interface DeviceRepositoryPort {
    fun create(device: Device): Device

    fun update(device: Device): Device

    fun findByUserIdAndInstallationId(userId: String, installationId: String): Device?
}
