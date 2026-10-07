package kindl.domain.user.repository

import kindl.domain.user.entity.Device
import org.springframework.data.jpa.repository.JpaRepository

interface DeviceRepository : JpaRepository<Device, Long> {
    fun findByUserIdAndInstallationId(userId: String, installationId: String): Device?
}
