package proov.domain.user

import proov.core.id.Tsids
import proov.core.type.Platform
import java.time.Instant

data class Device(
    val id: Long,
    val userId: String,
    val installationId: String,
    val spec: DeviceSpec,
    val lastSeenAt: Instant,
) {
    fun sync(spec: DeviceSpec, now: Instant) = copy(spec = spec, lastSeenAt = now)

    companion object {
        fun register(userId: String, installationId: String, spec: DeviceSpec, now: Instant) =
            Device(Tsids.nextLong(), userId, installationId, spec, now)
    }
}

data class DeviceSpec(
    val platform: Platform,
    val osVersion: String,
    val appVersion: String,
)
