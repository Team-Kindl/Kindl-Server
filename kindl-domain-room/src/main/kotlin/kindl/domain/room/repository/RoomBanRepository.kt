package kindl.domain.room.repository

import kindl.domain.room.entity.RoomBan
import org.springframework.data.jpa.repository.JpaRepository

interface RoomBanRepository : JpaRepository<RoomBan, Long>
