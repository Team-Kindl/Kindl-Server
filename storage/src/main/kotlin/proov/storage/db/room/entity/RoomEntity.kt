package proov.storage.db.room.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import proov.domain.room.InviteCode
import proov.domain.room.Room
import proov.domain.room.RoomName
import proov.domain.room.enums.RoomStatus
import proov.storage.db.common.BaseEntity
import java.time.Instant

@Entity
@Table(name = "rooms")
class RoomEntity(
    id: String,
    name: String,
    inviteCode: String,
    ownerUserId: String,
    memberCount: Int,
    status: RoomStatus,
    endedAt: Instant?,
    clientRequestId: String,
) : BaseEntity<String>() {

    @Id
    @Column(length = 13)
    private val id: String = id

    @Column(nullable = false, length = 60)
    var name: String = name
        protected set

    @Column(nullable = false, unique = true, length = 8)
    val inviteCode: String = inviteCode

    @Column(nullable = false, length = 13)
    var ownerUserId: String = ownerUserId
        protected set

    @Column(nullable = false)
    var memberCount: Int = memberCount
        protected set

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: RoomStatus = status
        protected set

    var endedAt: Instant? = endedAt
        protected set

    @Column(nullable = false, unique = true, length = 36)
    val clientRequestId: String = clientRequestId

    override fun getId(): String = id

    fun update(room: Room) {
        name = room.name.value
        ownerUserId = room.ownerUserId
        memberCount = room.memberCount
        status = room.status
        endedAt = room.endedAt
    }

    fun toDomain() = Room(
        id = id,
        name = RoomName.restore(name),
        inviteCode = InviteCode.parse(inviteCode)!!,
        ownerUserId = ownerUserId,
        memberCount = memberCount,
        status = status,
        endedAt = endedAt,
        clientRequestId = clientRequestId,
    )

    companion object {
        fun from(room: Room) = RoomEntity(
            id = room.id,
            name = room.name.value,
            inviteCode = room.inviteCode.value,
            ownerUserId = room.ownerUserId,
            memberCount = room.memberCount,
            status = room.status,
            endedAt = room.endedAt,
            clientRequestId = room.clientRequestId,
        )
    }
}
