package proov.domain.auth

import proov.core.id.Tsids
import java.time.Duration
import java.time.Instant

data class RefreshToken(
    val id: Long,
    val familyId: Long,
    val userId: String,
    val deviceId: Long,
    val tokenHash: String,
    val expiresAt: Instant,
    val rotatedAt: Instant? = null,
    val replacedById: Long? = null,
    val revokedAt: Instant? = null,
) {
    fun isUsable(now: Instant): Boolean = revokedAt == null && now < expiresAt

    fun isRotated(): Boolean = rotatedAt != null

    fun isWithinGrace(now: Instant, grace: Duration): Boolean = rotatedAt?.let { now < it.plus(grace) } ?: false

    fun rotateTo(next: RefreshToken, now: Instant): RefreshToken {
        check(rotatedAt == null) { "이미 회전된 refresh token입니다." }
        return copy(rotatedAt = now, replacedById = next.id)
    }

    companion object {
        fun issue(familyId: Long, userId: String, deviceId: Long, tokenHash: String, expiresAt: Instant) =
            RefreshToken(Tsids.nextLong(), familyId, userId, deviceId, tokenHash, expiresAt)
    }
}
