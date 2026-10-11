package proov.domain.user.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import proov.domain.user.Device
import proov.domain.user.command.DeviceCommand
import proov.domain.user.port.DeviceRepositoryPort
import java.time.Instant

@Service
class DeviceService(
    private val deviceRepositoryPort: DeviceRepositoryPort,
) {
    @Transactional
    fun sync(userId: String, command: DeviceCommand, now: Instant): Device {
        val device = deviceRepositoryPort.findByUserIdAndInstallationId(userId, command.installationId)
            ?: return deviceRepositoryPort.create(Device.register(userId, command.installationId, command.spec, now))
        return deviceRepositoryPort.update(device.sync(command.spec, now))
    }
}
