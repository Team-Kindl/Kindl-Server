package proov.domain.auth.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import proov.core.id.Tsids
import proov.domain.auth.IssuedToken
import proov.domain.auth.RefreshRotation
import proov.domain.auth.RefreshToken
import proov.domain.auth.RefreshTokenPolicy
import proov.domain.auth.port.RefreshTokenRepositoryPort
import java.security.MessageDigest
import java.security.SecureRandom
import java.time.Instant
import java.util.Base64
import java.util.HexFormat

@Service
class RefreshTokenService(
    private val refreshTokenRepositoryPort: RefreshTokenRepositoryPort,
    private val policy: RefreshTokenPolicy,
) {
    private val random = SecureRandom()

    @Transactional
    fun issue(userId: String, deviceId: Long, now: Instant): IssuedToken =
        newToken(Tsids.nextLong(), userId, deviceId, now).first

    @Transactional
    fun rotate(rawToken: String, now: Instant): RefreshRotation {
        val current = refreshTokenRepositoryPort.findByTokenHashForUpdate(hash(rawToken)) ?: return RefreshRotation.Invalid
        if (!current.isUsable(now)) return RefreshRotation.Invalid
        if (current.isRotated()) {
            if (current.isWithinGrace(now, policy.reuseGrace)) return RefreshRotation.Race
            refreshTokenRepositoryPort.revokeFamily(current.familyId, now)
            return RefreshRotation.Reused
        }

        val (issued, next) = newToken(current.familyId, current.userId, current.deviceId, now)
        refreshTokenRepositoryPort.update(current.rotateTo(next, now))
        return RefreshRotation.Rotated(current.userId, issued)
    }

    @Transactional
    fun revokeFamily(rawToken: String, userId: String, now: Instant) {
        val token = refreshTokenRepositoryPort.findByTokenHashForUpdate(hash(rawToken)) ?: return
        if (token.userId != userId) return
        refreshTokenRepositoryPort.revokeFamily(token.familyId, now)
    }

    private fun newToken(familyId: Long, userId: String, deviceId: Long, now: Instant): Pair<IssuedToken, RefreshToken> {
        val raw = ByteArray(TOKEN_BYTES).also(random::nextBytes).let(BASE64::encodeToString)
        val expiresAt = now.plus(policy.ttl)
        val saved = refreshTokenRepositoryPort.create(RefreshToken.issue(familyId, userId, deviceId, hash(raw), expiresAt))
        return IssuedToken(raw, expiresAt) to saved
    }

    companion object {
        private const val TOKEN_BYTES = 32
        private val BASE64: Base64.Encoder = Base64.getUrlEncoder().withoutPadding()

        fun hash(rawToken: String): String =
            HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(rawToken.toByteArray()))
    }
}
