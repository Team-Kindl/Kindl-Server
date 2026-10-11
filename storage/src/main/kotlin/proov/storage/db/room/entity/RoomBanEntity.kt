package proov.storage.db.room.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import proov.domain.room.RoomBan
import proov.storage.db.common.BaseEntity

@Entity
@Table(name = "room_bans", uniqueConstraints = [UniqueConstraint(columnNames = ["room_id", "user_id"])])
class RoomBanEntity(
    id: Long,
    roomId: String,
    userId: String,
    bannedByUserId: String,
) : BaseEntity<Long>() {

    @Id
    private val id: Long = id

    @Column(nullable = false, length = 13)
    val roomId: String = roomId

    @Column(nullable = false, length = 13)
    val userId: String = userId

    @Column(nullable = false, length = 13)
    val bannedByUserId: String = bannedByUserId

    override fun getId(): Long = id

    fun toDomain() = RoomBan(id, roomId, userId, bannedByUserId)

    companion object {
        fun from(ban: RoomBan) = RoomBanEntity(ban.id, ban.roomId, ban.userId, ban.bannedByUserId)
    }
}
