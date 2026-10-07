package kindl.domain.auth.service

import java.security.MessageDigest
import java.security.SecureRandom
import java.time.Instant
import java.util.Base64
import java.util.HexFormat
import kindl.core.id.Tsids
import kindl.domain.auth.dto.result.IssuedRefreshToken
import kindl.domain.auth.dto.result.RefreshRotation
import kindl.domain.auth.entity.RefreshToken
import kindl.domain.auth.repository.RefreshTokenRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 불투명한 256비트 refresh token을 쓸 때마다 새로 바꾼다 (RFC 9700의 회전 방식).
 * 폐기 결과가 커밋돼야 하므로 실패를 예외 대신 [RefreshRotation]으로 돌려준다.
 */
@Service
class RefreshTokenService(
    private val refreshTokens: RefreshTokenRepository,
    private val policy: RefreshTokenPolicy,
) {
    private val random = SecureRandom()

    @Transactional
    fun issue(userId: String, deviceId: Long, now: Instant): IssuedRefreshToken =
        issueInFamily(Tsids.nextLong(), userId, deviceId, now)

    @Transactional
    fun rotate(rawToken: String, now: Instant): RefreshRotation {
        val current = refreshTokens.findForUpdate(hash(rawToken)) ?: return RefreshRotation.Invalid
        if (!current.isUsable(now)) return RefreshRotation.Invalid
        if (current.isRotated()) {
            if (current.isWithinGrace(now, policy.reuseGrace)) return RefreshRotation.Race
            refreshTokens.revokeFamily(current.familyId, now)
            return RefreshRotation.Reused
        }

        val (issued, next) = newToken(current.familyId, current.userId, current.deviceId, now)
        current.rotateTo(next, now)
        return RefreshRotation.Rotated(current.userId, current.deviceId, issued)
    }

    /** 로그아웃: 이 토큰이 속한 회전 묶음(= 이 기기의 로그인)을 폐기한다. 모르는 토큰이면 조용히 끝낸다. */
    @Transactional
    fun revokeFamily(rawToken: String, userId: String, now: Instant) {
        val token = refreshTokens.findForUpdate(hash(rawToken)) ?: return
        if (token.userId != userId) return
        refreshTokens.revokeFamily(token.familyId, now)
    }

    private fun issueInFamily(familyId: Long, userId: String, deviceId: Long, now: Instant): IssuedRefreshToken =
        newToken(familyId, userId, deviceId, now).first

    private fun newToken(familyId: Long, userId: String, deviceId: Long, now: Instant): Pair<IssuedRefreshToken, RefreshToken> {
        val raw = ByteArray(TOKEN_BYTES).also(random::nextBytes).let(BASE64::encodeToString)
        val expiresAt = now.plus(policy.ttl)
        val saved = refreshTokens.save(RefreshToken.issue(familyId, userId, deviceId, hash(raw), expiresAt))
        return IssuedRefreshToken(raw, expiresAt) to saved
    }

    companion object {
        private const val TOKEN_BYTES = 32
        private val BASE64: Base64.Encoder = Base64.getUrlEncoder().withoutPadding()

        fun hash(rawToken: String): String =
            HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(rawToken.toByteArray()))
    }
}
