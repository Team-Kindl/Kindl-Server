package kindl.domain.room.service

import java.time.Instant
import kindl.core.error.CommonError
import kindl.core.error.KindlException
import kindl.domain.room.dto.command.CreateRoomCommand
import kindl.domain.room.dto.result.InvitePreviewResult
import kindl.domain.room.dto.result.JoinStatus
import kindl.domain.room.dto.result.LeaveResult
import kindl.domain.room.dto.result.MemberResult
import kindl.domain.room.dto.result.MyRoomResult
import kindl.domain.room.dto.result.RoomDetailResult
import kindl.domain.room.dto.result.RoomResult
import kindl.domain.room.entity.Room
import kindl.domain.room.entity.RoomBan
import kindl.domain.room.entity.RoomMember
import kindl.domain.room.enums.MemberRole
import kindl.domain.room.enums.RoomStatus
import kindl.domain.room.error.RoomError
import kindl.domain.room.repository.RoomBanRepository
import kindl.domain.room.repository.RoomMemberRepository
import kindl.domain.room.repository.RoomRepository
import kindl.domain.room.vo.InviteCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class RoomService(
    private val rooms: RoomRepository,
    private val members: RoomMemberRepository,
    private val bans: RoomBanRepository,
) {
    /**
     * 같은 Idempotency-Key면 처음 만든 모임을 그대로 돌려준다 (연타·네트워크 재시도).
     * 키가 모임 행과 같은 트랜잭션에 저장되므로, 응답 전에 서버가 죽어도 재시도가 모임을 하나 더 만들지 않는다.
     */
    @Transactional
    fun create(command: CreateRoomCommand, now: Instant): RoomResult {
        rooms.findByClientRequestId(command.clientRequestId)?.let { return RoomResult.from(it) }
        requireRoomSlot(command.userId)
        val room = rooms.save(Room.create(command.name, command.userId, newInviteCode(), command.clientRequestId))
        members.save(RoomMember.owner(requireNotNull(room.id), command.userId, now))
        return RoomResult.from(room)
    }

    @Transactional(readOnly = true)
    fun myRooms(userId: String): List<MyRoomResult> {
        val myMemberships = members.findAllByUserId(userId).associateBy { it.roomId }
        return rooms.findAllById(myMemberships.keys).map { room ->
            val member = myMemberships.getValue(requireNotNull(room.id))
            MyRoomResult(RoomResult.from(room), member.role, member.joinedAt, member.lastVerifiedAt)
        }
    }

    @Transactional(readOnly = true)
    fun detail(roomId: String, userId: String): RoomDetailResult {
        val room = rooms.findById(roomId).orElse(null) ?: throw notFound()
        val me = members.findByRoomIdAndUserId(roomId, userId) ?: throw notFound()
        val nextOwner = if (me.role == MemberRole.OWNER) members.findFirstByRoomIdAndUserIdNotOrderByJoinedAtAsc(roomId, userId) else null
        return RoomDetailResult(RoomResult.from(room), me.role, nextOwner?.userId)
    }

    @Transactional(readOnly = true)
    fun members(roomId: String, userId: String): List<MemberResult> {
        requireMember(roomId, userId)
        return members.findAllByRoomIdOrderByJoinedAtAsc(roomId).map(MemberResult::from)
    }

    @Transactional(readOnly = true)
    fun requireMember(roomId: String, userId: String) {
        if (!members.existsByRoomIdAndUserId(roomId, userId)) throw notFound()
    }

    /** 입장 불가도 오류가 아니라 상태로 돌려준다 — 미리보기는 사유와 모임 정보를 함께 그려야 해서 */
    @Transactional(readOnly = true)
    fun preview(rawCode: String, userId: String): InvitePreviewResult {
        val room = findByInviteCode(rawCode)
        val roomId = requireNotNull(room.id)
        val status = when {
            members.existsByRoomIdAndUserId(roomId, userId) -> JoinStatus.ALREADY_MEMBER
            bans.existsByRoomIdAndUserId(roomId, userId) -> JoinStatus.BANNED
            room.isEnded -> JoinStatus.ROOM_ENDED
            room.isFull -> JoinStatus.ROOM_FULL
            members.countByUserIdAndRoomStatus(userId, RoomStatus.ACTIVE) >= MAX_ROOMS_PER_USER -> JoinStatus.ROOM_LIMIT
            else -> JoinStatus.JOINABLE
        }
        val preview = members.findAllByRoomIdOrderByJoinedAtAsc(roomId).take(PREVIEW_MEMBER_COUNT).map { it.userId }
        return InvitePreviewResult(status, RoomResult.from(room), preview)
    }

    /**
     * 모임 행을 잠근 채(FOR UPDATE) 차단·종료·정원·내 모임 수를 확인하고 인원을 늘린다.
     * 동시에 여러 명이 눌러도 확인과 증가 사이에 끼어들 수 없어 정원을 넘지 않는다.
     * 같은 사람이 두 기기로 동시에 누르면 uk_member가 마지막에 막는다(→ ALREADY_JOINED).
     */
    @Transactional
    fun join(rawCode: String, userId: String, now: Instant): RoomResult {
        val code = InviteCode.parse(rawCode) ?: throw KindlException(RoomError.INVITE_NOT_FOUND)
        val room = rooms.findByInviteCodeForUpdate(code.value) ?: throw KindlException(RoomError.INVITE_NOT_FOUND)
        val roomId = requireNotNull(room.id)
        if (members.existsByRoomIdAndUserId(roomId, userId)) throw KindlException(RoomError.ALREADY_JOINED)
        if (bans.existsByRoomIdAndUserId(roomId, userId)) throw KindlException(RoomError.BANNED_FROM_ROOM)
        room.addMember()
        requireRoomSlot(userId)
        members.saveAndFlush(RoomMember.member(roomId, userId, now))
        return RoomResult.from(room)
    }

    /** 멤버 행만 다룬다. 그 모임의 내 공약·인증 삭제는 Facade가 같은 트랜잭션에서 이어서 한다 */
    @Transactional
    fun leave(roomId: String, userId: String, now: Instant): LeaveResult {
        val room = rooms.findByIdForUpdate(roomId) ?: throw notFound()
        val me = members.findByRoomIdAndUserId(roomId, userId) ?: throw notFound()
        return removeMember(room, me, now)
    }

    @Transactional
    fun kick(roomId: String, ownerUserId: String, targetUserId: String, now: Instant) {
        val room = rooms.findByIdForUpdate(roomId) ?: throw notFound()
        requireMember(roomId, ownerUserId)
        room.requireOwner(ownerUserId)
        room.requireActive()
        if (ownerUserId == targetUserId) throw KindlException(RoomError.CANNOT_KICK_SELF)
        val target = members.findByRoomIdAndUserId(roomId, targetUserId) ?: throw notFound()
        removeMember(room, target, now)
        bans.save(RoomBan.of(roomId, targetUserId, ownerUserId))
    }

    /** 종료하고 멤버 목록을 돌려준다. 멤버들의 진행 중 공약 마감은 Facade가 이어서 한다 */
    @Transactional
    fun end(roomId: String, userId: String, now: Instant): Pair<RoomResult, List<String>> {
        val room = rooms.findByIdForUpdate(roomId) ?: throw notFound()
        requireMember(roomId, userId)
        room.requireOwner(userId)
        room.end(now)
        return RoomResult.from(room) to members.findAllByRoomIdOrderByJoinedAtAsc(roomId).map { it.userId }
    }

    private fun removeMember(room: Room, member: RoomMember, now: Instant): LeaveResult {
        val roomId = requireNotNull(room.id)
        member.softDelete(now)
        room.removeMember()
        if (member.role != MemberRole.OWNER) return LeaveResult(roomDeleted = false, newOwnerUserId = null)

        val next = members.findFirstByRoomIdAndUserIdNotOrderByJoinedAtAsc(roomId, member.userId)
        if (next == null) {
            room.softDelete(now)
            return LeaveResult(roomDeleted = true, newOwnerUserId = null)
        }
        next.promoteToOwner()
        room.handOverTo(next.userId)
        return LeaveResult(roomDeleted = false, newOwnerUserId = next.userId)
    }

    private fun requireRoomSlot(userId: String) {
        if (members.countByUserIdAndRoomStatus(userId, RoomStatus.ACTIVE) >= MAX_ROOMS_PER_USER) {
            throw KindlException(RoomError.ROOM_LIMIT_EXCEEDED)
        }
    }

    private fun findByInviteCode(rawCode: String): Room {
        val code = InviteCode.parse(rawCode) ?: throw KindlException(RoomError.INVITE_NOT_FOUND)
        return rooms.findByInviteCode(code.value) ?: throw KindlException(RoomError.INVITE_NOT_FOUND)
    }

    // 32^8 ≈ 1.1조라 충돌은 드물지만 0이 아니다. 지워진 모임의 코드까지 피해서 다시 뽑는다
    private fun newInviteCode(): InviteCode {
        repeat(INVITE_CODE_ATTEMPTS) {
            val code = InviteCode.generate()
            if (!rooms.existsByInviteCodeIncludingDeleted(code.value)) return code
        }
        throw KindlException(CommonError.CONFLICT_STATE, "초대 코드 생성 실패")
    }

    // 존재 여부를 알려 주지 않으려고 403 대신 404
    private fun notFound() = KindlException(CommonError.RESOURCE_NOT_FOUND)

    companion object {
        const val MAX_ROOMS_PER_USER = 10L
        private const val PREVIEW_MEMBER_COUNT = 5
        private const val INVITE_CODE_ATTEMPTS = 3
    }
}
