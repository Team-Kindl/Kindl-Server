package kindl.domain.room.repository

import kindl.domain.room.dto.projection.MyRoomProjection

interface RoomMemberRepositoryCustom {
    fun findMyRooms(userId: String): List<MyRoomProjection>
}
