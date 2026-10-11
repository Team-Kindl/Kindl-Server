package proov.storage.db.user.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import proov.core.type.Platform
import proov.domain.user.Device
import proov.domain.user.DeviceSpec
import proov.storage.db.common.BaseEntity
import java.time.Instant

@Entity
@Table(name = "devices", uniqueConstraints = [UniqueConstraint(columnNames = ["user_id", "installation_id"])])
class DeviceEntity(
    id: Long,
    userId: String,
    installationId: String,
    platform: Platform,
    osVersion: String,
    appVersion: String,
    lastSeenAt: Instant,
) : BaseEntity<Long>() {

    @Id
    private val id: Long = id

    @Column(nullable = false, length = 13)
    val userId: String = userId

    @Column(nullable = false, length = 64)
    val installationId: String = installationId

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var platform: Platform = platform
        protected set

    @Column(nullable = false, length = 20)
    var osVersion: String = osVersion
        protected set

    @Column(nullable = false, length = 20)
    var appVersion: String = appVersion
        protected set

    @Column(nullable = false)
    var lastSeenAt: Instant = lastSeenAt
        protected set

    override fun getId(): Long = id

    fun update(device: Device) {
        platform = device.spec.platform
        osVersion = device.spec.osVersion
        appVersion = device.spec.appVersion
        lastSeenAt = device.lastSeenAt
    }

    fun toDomain() = Device(id, userId, installationId, DeviceSpec(platform, osVersion, appVersion), lastSeenAt)

    companion object {
        fun from(device: Device) = DeviceEntity(
            id = device.id,
            userId = device.userId,
            installationId = device.installationId,
            platform = device.spec.platform,
            osVersion = device.spec.osVersion,
            appVersion = device.spec.appVersion,
            lastSeenAt = device.lastSeenAt,
        )
    }
}
