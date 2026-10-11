package proov.storage.db.user.repository

import org.springframework.data.jpa.repository.JpaRepository
import proov.storage.db.user.entity.DeviceEntity

interface DeviceJpaRepository : JpaRepository<DeviceEntity, Long> {
    fun findByUserIdAndInstallationId(userId: String, installationId: String): DeviceEntity?
}
