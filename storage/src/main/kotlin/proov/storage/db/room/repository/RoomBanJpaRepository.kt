package proov.storage.db.room.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import proov.storage.db.room.entity.RoomBanEntity

interface RoomBanJpaRepository : JpaRepository<RoomBanEntity, Long> {

    fun existsByRoomIdAndUserId(roomId: String, userId: String): Boolean

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from RoomBanEntity b where b.roomId = :roomId")
    fun deleteAllByRoomId(roomId: String): Int
}
