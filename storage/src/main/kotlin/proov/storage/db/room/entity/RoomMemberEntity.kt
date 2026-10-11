package proov.storage.db.room.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import proov.domain.room.RoomMember
import proov.domain.room.enums.MemberRole
import proov.storage.db.common.BaseEntity
import java.time.Instant

@Entity
@Table(name = "room_members", uniqueConstraints = [UniqueConstraint(columnNames = ["room_id", "user_id"])])
class RoomMemberEntity(
    id: Long,
    roomId: String,
    userId: String,
    role: MemberRole,
    joinedAt: Instant,
) : BaseEntity<Long>() {

    @Id
    private val id: Long = id

    @Column(nullable = false, length = 13)
    val roomId: String = roomId

    @Column(nullable = false, length = 13)
    val userId: String = userId

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var role: MemberRole = role
        protected set

    @Column(nullable = false)
    val joinedAt: Instant = joinedAt

    override fun getId(): Long = id

    fun update(member: RoomMember) {
        role = member.role
    }

    fun toDomain() = RoomMember(id, roomId, userId, role, joinedAt)

    companion object {
        fun from(member: RoomMember) = RoomMemberEntity(member.id, member.roomId, member.userId, member.role, member.joinedAt)
    }
}
