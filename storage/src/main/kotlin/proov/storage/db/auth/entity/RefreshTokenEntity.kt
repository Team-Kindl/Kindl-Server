package proov.storage.db.auth.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import proov.domain.auth.RefreshToken
import proov.storage.db.common.BaseEntity
import java.time.Instant

@Entity
@Table(name = "refresh_tokens")
class RefreshTokenEntity(
    id: Long,
    familyId: Long,
    userId: String,
    deviceId: Long,
    tokenHash: String,
    expiresAt: Instant,
) : BaseEntity<Long>() {

    @Id
    private val id: Long = id

    @Column(nullable = false)
    val familyId: Long = familyId

    @Column(nullable = false, length = 13)
    val userId: String = userId

    @Column(nullable = false)
    val deviceId: Long = deviceId

    @Column(nullable = false, unique = true, length = 64)
    val tokenHash: String = tokenHash

    @Column(nullable = false)
    val expiresAt: Instant = expiresAt

    var rotatedAt: Instant? = null
        protected set

    var replacedById: Long? = null
        protected set

    var revokedAt: Instant? = null
        protected set

    override fun getId(): Long = id

    fun update(token: RefreshToken) {
        rotatedAt = token.rotatedAt
        replacedById = token.replacedById
        revokedAt = token.revokedAt
    }

    fun toDomain() = RefreshToken(id, familyId, userId, deviceId, tokenHash, expiresAt, rotatedAt, replacedById, revokedAt)

    companion object {
        fun from(token: RefreshToken) =
            RefreshTokenEntity(token.id, token.familyId, token.userId, token.deviceId, token.tokenHash, token.expiresAt)
                .apply { update(token) }
    }
}
