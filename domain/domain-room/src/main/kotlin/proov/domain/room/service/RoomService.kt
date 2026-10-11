package proov.domain.room.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import proov.core.exception.ProovException
import proov.core.extension.orThrow
import proov.domain.room.InviteCode
import proov.domain.room.InvitePreview
import proov.domain.room.LeaveResult
import proov.domain.room.MyRoom
import proov.domain.room.Room
import proov.domain.room.RoomBan
import proov.domain.room.RoomDetail
import proov.domain.room.RoomMember
import proov.domain.room.RoomName
import proov.domain.room.enums.JoinStatus
import proov.domain.room.enums.MemberRole
import proov.domain.room.enums.RoomStatus
import proov.domain.room.exception.RoomFailure
import proov.domain.room.port.RoomBanRepositoryPort
import proov.domain.room.port.RoomMemberRepositoryPort
import proov.domain.room.port.RoomRepositoryPort
import java.time.Instant

@Service
class RoomService(
    private val roomRepositoryPort: RoomRepositoryPort,
    private val roomMemberRepositoryPort: RoomMemberRepositoryPort,
    private val roomBanRepositoryPort: RoomBanRepositoryPort,
) {
    @Transactional
    fun create(userId: String, name: RoomName, clientRequestId: String, now: Instant): Room {
        roomRepositoryPort.findByClientRequestId(clientRequestId)?.let { return it }
        requireRoomSlot(userId)
        val room = roomRepositoryPort.create(Room.create(name, userId, newInviteCode(), clientRequestId))
        roomMemberRepositoryPort.create(RoomMember.owner(room.id, userId, now))
        return room
    }

    fun myRooms(userId: String): List<MyRoom> = roomMemberRepositoryPort.findMyRooms(userId)

    fun detail(roomId: String, userId: String): RoomDetail {
        val room = roomRepositoryPort.findById(roomId).orThrow(RoomFailure.ROOM_NOT_FOUND)
        val me = roomMemberRepositoryPort.findByRoomIdAndUserId(roomId, userId).orThrow(RoomFailure.ROOM_NOT_FOUND)
        val nextOwner = if (me.role == MemberRole.OWNER) roomMemberRepositoryPort.findFirstOtherMember(roomId, userId) else null
        return RoomDetail(room, me.role, nextOwner?.userId)
    }

    fun members(roomId: String, userId: String): List<RoomMember> {
        requireMember(roomId, userId)
        return roomMemberRepositoryPort.findAllByRoomIdOrderByJoinedAt(roomId)
    }

    fun preview(rawCode: String, userId: String): InvitePreview {
        val code = InviteCode.parse(rawCode).orThrow(RoomFailure.INVITE_NOT_FOUND)
        val room = roomRepositoryPort.findByInviteCode(code.value).orThrow(RoomFailure.INVITE_NOT_FOUND)
        val status = when {
            roomMemberRepositoryPort.existsByRoomIdAndUserId(room.id, userId) -> JoinStatus.ALREADY_MEMBER
            roomBanRepositoryPort.existsByRoomIdAndUserId(room.id, userId) -> JoinStatus.BANNED
            room.isEnded -> JoinStatus.ROOM_ENDED
            room.isFull -> JoinStatus.ROOM_FULL
            !hasRoomSlot(userId) -> JoinStatus.ROOM_LIMIT
            else -> JoinStatus.JOINABLE
        }
        val previewMemberIds = roomMemberRepositoryPort.findAllByRoomIdOrderByJoinedAt(room.id)
            .take(PREVIEW_MEMBER_COUNT)
            .map { it.userId }
        return InvitePreview(status, room, previewMemberIds)
    }

    @Transactional
    fun join(rawCode: String, userId: String, now: Instant): Room {
        val code = InviteCode.parse(rawCode).orThrow(RoomFailure.INVITE_NOT_FOUND)
        val room = roomRepositoryPort.findByInviteCodeForUpdate(code.value).orThrow(RoomFailure.INVITE_NOT_FOUND)
        if (roomMemberRepositoryPort.existsByRoomIdAndUserId(room.id, userId)) throw ProovException(RoomFailure.ALREADY_JOINED)
        if (roomBanRepositoryPort.existsByRoomIdAndUserId(room.id, userId)) throw ProovException(RoomFailure.BANNED_FROM_ROOM)
        val joined = room.addMember()
        requireRoomSlot(userId)
        roomMemberRepositoryPort.create(RoomMember.member(room.id, userId, now))
        return roomRepositoryPort.update(joined)
    }

    @Transactional
    fun leave(roomId: String, userId: String): LeaveResult {
        val room = lockRoom(roomId)
        val me = roomMemberRepositoryPort.findByRoomIdAndUserId(roomId, userId).orThrow(RoomFailure.ROOM_NOT_FOUND)
        return removeMember(room, me)
    }

    @Transactional
    fun kick(roomId: String, ownerUserId: String, targetUserId: String) {
        val room = lockRoom(roomId)
        requireMember(roomId, ownerUserId)
        room.requireOwner(ownerUserId)
        room.requireActive()
        if (ownerUserId == targetUserId) throw ProovException(RoomFailure.CANNOT_KICK_SELF)
        val target = roomMemberRepositoryPort.findByRoomIdAndUserId(roomId, targetUserId).orThrow(RoomFailure.ROOM_NOT_FOUND)
        removeMember(room, target)
        roomBanRepositoryPort.create(RoomBan.of(roomId, targetUserId, ownerUserId))
    }

    @Transactional
    fun end(roomId: String, userId: String, now: Instant): Room {
        val room = lockRoom(roomId)
        requireMember(roomId, userId)
        room.requireOwner(userId)
        return roomRepositoryPort.update(room.end(now))
    }

    private fun removeMember(room: Room, member: RoomMember): LeaveResult {
        roomMemberRepositoryPort.delete(member)
        val remaining = room.removeMember()
        if (member.role != MemberRole.OWNER) {
            roomRepositoryPort.update(remaining)
            return LeaveResult(roomDeleted = false, newOwnerUserId = null)
        }

        val next = roomMemberRepositoryPort.findFirstOtherMember(room.id, member.userId)
        if (next == null) {
            roomBanRepositoryPort.deleteAllByRoomId(room.id)
            roomRepositoryPort.delete(room.id)
            return LeaveResult(roomDeleted = true, newOwnerUserId = null)
        }
        roomMemberRepositoryPort.update(next.promoteToOwner())
        roomRepositoryPort.update(remaining.handOverTo(next.userId))
        return LeaveResult(roomDeleted = false, newOwnerUserId = next.userId)
    }

    private fun lockRoom(roomId: String): Room = roomRepositoryPort.findByIdForUpdate(roomId).orThrow(RoomFailure.ROOM_NOT_FOUND)

    private fun requireMember(roomId: String, userId: String) {
        if (!roomMemberRepositoryPort.existsByRoomIdAndUserId(roomId, userId)) throw ProovException(RoomFailure.ROOM_NOT_FOUND)
    }

    private fun hasRoomSlot(userId: String): Boolean =
        roomMemberRepositoryPort.countByUserIdAndRoomStatus(userId, RoomStatus.ACTIVE) < Room.MAX_ROOMS_PER_USER

    private fun requireRoomSlot(userId: String) {
        if (!hasRoomSlot(userId)) throw ProovException(RoomFailure.ROOM_LIMIT_EXCEEDED)
    }

    private fun newInviteCode(): InviteCode {
        repeat(INVITE_CODE_ATTEMPTS) {
            val code = InviteCode.generate()
            if (!roomRepositoryPort.existsByInviteCode(code.value)) return code
        }
        throw ProovException(RoomFailure.INVITE_CODE_EXHAUSTED)
    }

    companion object {
        private const val PREVIEW_MEMBER_COUNT = 5
        private const val INVITE_CODE_ATTEMPTS = 3
    }
}
