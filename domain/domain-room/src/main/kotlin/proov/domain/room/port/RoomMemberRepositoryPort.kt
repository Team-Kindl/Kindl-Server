package proov.domain.room.port

import proov.domain.room.MyRoom
import proov.domain.room.RoomMember
import proov.domain.room.enums.RoomStatus

interface RoomMemberRepositoryPort {
    fun create(member: RoomMember): RoomMember

    fun update(member: RoomMember): RoomMember

    fun delete(member: RoomMember)

    fun findByRoomIdAndUserId(roomId: String, userId: String): RoomMember?

    fun existsByRoomIdAndUserId(roomId: String, userId: String): Boolean

    fun findAllByRoomIdOrderByJoinedAt(roomId: String): List<RoomMember>

    fun findFirstOtherMember(roomId: String, userId: String): RoomMember?

    fun countByUserIdAndRoomStatus(userId: String, status: RoomStatus): Long

    fun findMyRooms(userId: String): List<MyRoom>
}
