package kindl.domain.user.service

import kindl.domain.user.command.DeviceCommand
import kindl.domain.user.entity.Device
import kindl.domain.user.repository.DeviceRepository
import kindl.domain.user.result.DeviceResult
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class DeviceService(
    private val devices: DeviceRepository,
) {
    /** 로그인·가입·앱 실행 때마다 기기 정보와 권한 상태를 맞춘다. */
    @Transactional
    fun upsert(userId: String, command: DeviceCommand, now: Instant): DeviceResult {
        val device = devices.findByUserIdAndInstallationId(userId, command.installationId)
            ?.apply { sync(command.spec, now) }
            ?: devices.save(Device.register(userId, command.installationId, command.spec, now))
        return DeviceResult.from(device)
    }
}
