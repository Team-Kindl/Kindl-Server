package proov.storage.db.room.querydsl

import proov.storage.db.room.projection.MyRoomProjection

interface RoomMemberQuerydslRepository {
    fun findMyRooms(userId: String): List<MyRoomProjection>
}
