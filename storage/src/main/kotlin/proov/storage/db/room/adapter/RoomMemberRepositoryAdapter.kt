package proov.storage.db.room.adapter

import org.springframework.stereotype.Repository
import proov.domain.room.InviteCode
import proov.domain.room.MyRoom
import proov.domain.room.Room
import proov.domain.room.RoomMember
import proov.domain.room.RoomName
import proov.domain.room.enums.RoomStatus
import proov.domain.room.port.RoomMemberRepositoryPort
import proov.storage.db.room.entity.RoomMemberEntity
import proov.storage.db.room.projection.MyRoomProjection
import proov.storage.db.room.querydsl.RoomMemberQuerydslRepository
import proov.storage.db.room.repository.RoomMemberJpaRepository

@Repository
class RoomMemberRepositoryAdapter(
    private val roomMemberJpaRepository: RoomMemberJpaRepository,
    private val roomMemberQuerydslRepository: RoomMemberQuerydslRepository,
) : RoomMemberRepositoryPort {

    override fun create(member: RoomMember): RoomMember = roomMemberJpaRepository.save(RoomMemberEntity.from(member)).toDomain()

    override fun update(member: RoomMember): RoomMember =
        roomMemberJpaRepository.getReferenceById(member.id).apply { update(member) }.toDomain()

    override fun delete(member: RoomMember) = roomMemberJpaRepository.deleteById(member.id)

    override fun findByRoomIdAndUserId(roomId: String, userId: String): RoomMember? =
        roomMemberJpaRepository.findByRoomIdAndUserId(roomId, userId)?.toDomain()

    override fun existsByRoomIdAndUserId(roomId: String, userId: String): Boolean =
        roomMemberJpaRepository.existsByRoomIdAndUserId(roomId, userId)

    override fun findAllByRoomIdOrderByJoinedAt(roomId: String): List<RoomMember> =
        roomMemberJpaRepository.findAllByRoomIdOrderByJoinedAtAsc(roomId).map(RoomMemberEntity::toDomain)

    override fun findFirstOtherMember(roomId: String, userId: String): RoomMember? =
        roomMemberJpaRepository.findFirstByRoomIdAndUserIdNotOrderByJoinedAtAsc(roomId, userId)?.toDomain()

    override fun countByUserIdAndRoomStatus(userId: String, status: RoomStatus): Long =
        roomMemberJpaRepository.countByUserIdAndRoomStatus(userId, status)

    override fun findMyRooms(userId: String): List<MyRoom> =
        roomMemberQuerydslRepository.findMyRooms(userId).map(::toMyRoom)

    private fun toMyRoom(row: MyRoomProjection) = MyRoom(
        room = Room(
            id = row.roomId,
            name = RoomName.restore(row.name),
            inviteCode = InviteCode.parse(row.inviteCode)!!,
            ownerUserId = row.ownerUserId,
            memberCount = row.memberCount,
            status = row.status,
            endedAt = row.endedAt,
            clientRequestId = row.clientRequestId,
        ),
        myRole = row.myRole,
        joinedAt = row.joinedAt,
    )
}
