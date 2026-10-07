package kindl.domain.room.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import kindl.support.entity.BaseSoftDeleteEntity
import kindl.support.id.TsidId
import org.hibernate.annotations.SQLRestriction

/** 모임장이 내보낸 멤버. 같은 모임에 다시 들어올 수 없다. */
@Entity
@Table(name = "room_bans")
@SQLRestriction("deleted_at IS NULL")
class RoomBan private constructor(
    roomId: String,
    userId: String,
    bannedByUserId: String,
) : BaseSoftDeleteEntity() {

    @Id @TsidId
    var id: Long? = null
        protected set

    @Column(nullable = false, length = 13, updatable = false)
    val roomId: String = roomId

    @Column(nullable = false, length = 13, updatable = false)
    val userId: String = userId

    @Column(nullable = false, length = 13, updatable = false)
    val bannedByUserId: String = bannedByUserId

    companion object {
        fun of(roomId: String, userId: String, bannedByUserId: String) = RoomBan(roomId, userId, bannedByUserId)
    }
}
