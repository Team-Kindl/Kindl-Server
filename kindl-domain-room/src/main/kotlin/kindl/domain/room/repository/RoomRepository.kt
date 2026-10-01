package kindl.domain.room.repository

import kindl.domain.room.entity.Room
import org.springframework.data.jpa.repository.JpaRepository

interface RoomRepository : JpaRepository<Room, String>
