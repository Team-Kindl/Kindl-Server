package proov.storage.db.user.adapter

import org.springframework.stereotype.Repository
import proov.domain.user.Device
import proov.domain.user.port.DeviceRepositoryPort
import proov.storage.db.user.entity.DeviceEntity
import proov.storage.db.user.repository.DeviceJpaRepository

@Repository
class DeviceRepositoryAdapter(
    private val deviceJpaRepository: DeviceJpaRepository,
) : DeviceRepositoryPort {

    override fun create(device: Device): Device = deviceJpaRepository.save(DeviceEntity.from(device)).toDomain()

    override fun update(device: Device): Device =
        deviceJpaRepository.getReferenceById(device.id).apply { update(device) }.toDomain()

    override fun findByUserIdAndInstallationId(userId: String, installationId: String): Device? =
        deviceJpaRepository.findByUserIdAndInstallationId(userId, installationId)?.toDomain()
}
