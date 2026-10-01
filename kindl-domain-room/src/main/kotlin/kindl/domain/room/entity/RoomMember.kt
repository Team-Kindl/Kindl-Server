package kindl.domain.room.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import kindl.support.entity.BaseSoftDeleteEntity
import kindl.support.id.TsidId
import org.hibernate.annotations.SQLRestriction
import java.time.Instant

/** 나갔다 다시 오면 새 행이다. */
@Entity
@Table(name = "room_members")
@SQLRestriction("deleted_at IS NULL")
class RoomMember private constructor(
    roomId: String,
    userId: String,
    role: MemberRole,
    joinedAt: Instant,
) : BaseSoftDeleteEntity() {

    @Id @TsidId
    var id: Long? = null
        protected set

    @Column(nullable = false, length = 13, updatable = false)
    val roomId: String = roomId

    @Column(nullable = false, length = 13, updatable = false)
    val userId: String = userId

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var role: MemberRole = role
        protected set

    // 모임장 승계 순서
    @Column(nullable = false, updatable = false)
    val joinedAt: Instant = joinedAt

    // 모임 목록 정렬용
    var lastVerifiedAt: Instant? = null
        protected set

    fun promoteToOwner() {
        role = MemberRole.OWNER
    }

    fun markVerified(now: Instant) {
        lastVerifiedAt = now
    }

    companion object {
        fun owner(roomId: String, userId: String, now: Instant) = RoomMember(roomId, userId, MemberRole.OWNER, now)

        fun member(roomId: String, userId: String, now: Instant) = RoomMember(roomId, userId, MemberRole.MEMBER, now)
    }
}
