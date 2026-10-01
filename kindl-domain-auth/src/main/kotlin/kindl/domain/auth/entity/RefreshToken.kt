package kindl.domain.auth.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import kindl.support.entity.BaseCreatedEntity
import kindl.support.id.TsidId
import java.time.Duration
import java.time.Instant

/**
 * 원문은 저장하지 않고 SHA-256만 둔다. soft delete 대신 revokedAt + 배치 물리 삭제.
 * 로그인 한 번에서 이어진 회전 묶음은 familyId로 묶여, 재사용이 감지되면 묶음 전체를 폐기한다.
 */
@Entity
@Table(name = "refresh_tokens")
class RefreshToken private constructor(
    familyId: Long,
    userId: String,
    deviceId: Long,
    tokenHash: String,
    expiresAt: Instant,
) : BaseCreatedEntity() {

    @Id @TsidId
    var id: Long? = null
        protected set

    @Column(nullable = false, updatable = false)
    val familyId: Long = familyId

    @Column(nullable = false, length = 13, updatable = false)
    val userId: String = userId

    @Column(nullable = false, updatable = false)
    val deviceId: Long = deviceId

    @Column(nullable = false, updatable = false, columnDefinition = "char(64)")
    val tokenHash: String = tokenHash

    @Column(nullable = false, updatable = false)
    val expiresAt: Instant = expiresAt

    // 새 토큰으로 바뀐 시각. 재사용 유예 판단 기준
    var rotatedAt: Instant? = null
        protected set

    var replacedById: Long? = null
        protected set

    var revokedAt: Instant? = null
        protected set

    fun isUsable(now: Instant): Boolean = revokedAt == null && now < expiresAt

    fun isRotated(): Boolean = rotatedAt != null

    fun isWithinGrace(now: Instant, grace: Duration): Boolean =
        rotatedAt?.let { now < it.plus(grace) } ?: false

    fun rotateTo(next: RefreshToken, now: Instant) {
        check(rotatedAt == null) { "이미 회전된 refresh token입니다." }
        rotatedAt = now
        replacedById = requireNotNull(next.id)
    }

    fun revoke(now: Instant) {
        if (revokedAt == null) revokedAt = now
    }

    companion object {
        fun issue(familyId: Long, userId: String, deviceId: Long, tokenHash: String, expiresAt: Instant) =
            RefreshToken(familyId, userId, deviceId, tokenHash, expiresAt)
    }
}
