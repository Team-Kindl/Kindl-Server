package kindl.domain.room.service

import kindl.core.error.CommonError
import kindl.core.error.KindlException
import kindl.core.extension.orThrow
import kindl.domain.room.dto.result.InvitePreviewResult
import kindl.domain.room.dto.result.JoinStatus
import kindl.domain.room.dto.result.MemberResult
import kindl.domain.room.dto.result.MyRoomResult
import kindl.domain.room.dto.result.RoomDetailResult
import kindl.domain.room.dto.result.RoomResult
import kindl.domain.room.entity.Room
import kindl.domain.room.enums.MemberRole
import kindl.domain.room.enums.RoomStatus
import kindl.domain.room.error.RoomError
import kindl.domain.room.repository.RoomBanRepository
import kindl.domain.room.repository.RoomMemberRepository
import kindl.domain.room.repository.RoomRepository
import kindl.domain.room.vo.InviteCode
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

/**
 * @Transactional을 걸지 않는다. 쿼리 하나짜리는 레포지토리의 readOnly 트랜잭션으로 충분하고,
 * 여러 쿼리를 한 시점으로 읽는 화면은 RoomFacade가 readOnly 트랜잭션을 열어 그 안에서 부른다.
 * 존재 여부를 알려 주지 않으려고 남의 모임은 403 대신 404로 답한다.
 */
@Service
class RoomQueryService(
    private val rooms: RoomRepository,
    private val members: RoomMemberRepository,
    private val bans: RoomBanRepository,
) {
    fun myRooms(userId: String): List<MyRoomResult> =
        members.findMyRooms(userId).map(MyRoomResult::from)

    fun detail(roomId: String, userId: String): RoomDetailResult {
        val room = rooms.findByIdOrNull(roomId).orThrow(CommonError.RESOURCE_NOT_FOUND)
        val me = members.findByRoomIdAndUserId(roomId, userId).orThrow(CommonError.RESOURCE_NOT_FOUND)
        val nextOwner = if (me.role == MemberRole.OWNER) members.findFirstByRoomIdAndUserIdNotOrderByJoinedAtAsc(roomId, userId) else null
        return RoomDetailResult(RoomResult.from(room), me.role, nextOwner?.userId)
    }

    fun members(roomId: String, userId: String): List<MemberResult> {
        if (!members.existsByRoomIdAndUserId(roomId, userId)) throw KindlException(CommonError.RESOURCE_NOT_FOUND)
        return members.findAllByRoomIdOrderByJoinedAtAsc(roomId).map(MemberResult::from)
    }

    /** 입장 불가도 오류가 아니라 상태로 돌려준다 — 미리보기는 사유와 모임 정보를 함께 그려야 해서 */
    fun preview(rawCode: String, userId: String): InvitePreviewResult {
        val code = InviteCode.parse(rawCode).orThrow(RoomError.INVITE_NOT_FOUND)
        val room = rooms.findByInviteCode(code.value).orThrow(RoomError.INVITE_NOT_FOUND)
        val roomId = requireNotNull(room.id)
        val status = when {
            members.existsByRoomIdAndUserId(roomId, userId) -> JoinStatus.ALREADY_MEMBER
            bans.existsByRoomIdAndUserId(roomId, userId) -> JoinStatus.BANNED
            room.isEnded -> JoinStatus.ROOM_ENDED
            room.isFull -> JoinStatus.ROOM_FULL
            members.countByUserIdAndRoomStatus(userId, RoomStatus.ACTIVE) >= Room.MAX_ROOMS_PER_USER -> JoinStatus.ROOM_LIMIT
            else -> JoinStatus.JOINABLE
        }
        val preview = members.findAllByRoomIdOrderByJoinedAtAsc(roomId).take(PREVIEW_MEMBER_COUNT).map { it.userId }
        return InvitePreviewResult(status, RoomResult.from(room), preview)
    }

    companion object {
        private const val PREVIEW_MEMBER_COUNT = 5
    }
}
