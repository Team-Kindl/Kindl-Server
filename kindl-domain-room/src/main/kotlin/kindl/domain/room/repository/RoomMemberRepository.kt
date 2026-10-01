package kindl.domain.room.repository

import kindl.domain.room.entity.RoomMember
import org.springframework.data.jpa.repository.JpaRepository

interface RoomMemberRepository : JpaRepository<RoomMember, Long>
