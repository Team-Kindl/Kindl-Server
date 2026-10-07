package kindl.api.room.facade

import java.time.Instant
import kindl.api.room.dto.response.EndRoomResponse
import kindl.api.room.dto.response.InvitePreviewResponse
import kindl.api.room.dto.response.LeaveRoomResponse
import kindl.api.room.dto.response.MemberResponse
import kindl.api.room.dto.response.MembersResponse
import kindl.api.room.dto.response.MyRoomsResponse
import kindl.api.room.dto.response.RoomCardResponse
import kindl.api.room.dto.response.RoomDetailResponse
import kindl.api.room.dto.response.UserRef
import kindl.core.time.ServiceClock
import kindl.domain.promise.service.PromiseCommandService
import kindl.domain.promise.service.PromiseQueryService
import kindl.domain.room.dto.result.MyRoomResult
import kindl.domain.room.enums.RoomStatus
import kindl.domain.room.service.RoomCommandService
import kindl.domain.room.service.RoomQueryService
import kindl.domain.user.service.UserQueryService
import kindl.domain.verification.service.VerificationCommandService
import kindl.domain.verification.service.VerificationQueryService
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

/**
 * 모임 + 공약 + 인증 + 유저를 엮는 유스케이스. 도메인 서비스끼리는 서로 모른다.
 * 쓰기는 Facade가 트랜잭션 경계를 갖고, 서비스의 @Transactional(REQUIRED)은 여기에 합류한다.
 * 여러 쿼리를 한 화면으로 묶는 조회는 readOnly 트랜잭션으로 같은 시점을 읽는다. Query 서비스는 트랜잭션을 열지 않는다.
 */
@Component
class RoomFacade(
    private val roomQueryService: RoomQueryService,
    private val roomCommandService: RoomCommandService,
    private val promiseQueryService: PromiseQueryService,
    private val promiseCommandService: PromiseCommandService,
    private val verificationQueryService: VerificationQueryService,
    private val verificationCommandService: VerificationCommandService,
    private val userQueryService: UserQueryService,
    private val clock: ServiceClock,
) {
    @Transactional(readOnly = true)
    fun myRooms(userId: String, status: RoomStatus): MyRoomsResponse {
        val all = roomQueryService.myRooms(userId)
        val counts = promiseQueryService.activeCounts(all.map { it.room.roomId })
        val ordering = if (status == RoomStatus.ACTIVE) ACTIVE_ORDER else ENDED_ORDER
        return MyRoomsResponse(
            rooms = all.filter { it.room.status == status }.sortedWith(ordering).map {
                RoomCardResponse(
                    roomId = it.room.roomId,
                    name = it.room.name,
                    imageUrl = null,
                    memberCount = it.room.memberCount,
                    capacity = it.room.capacity,
                    activePromiseCount = counts.ofRoom(it.room.roomId),
                    myPromiseCount = counts.of(it.room.roomId, userId),
                    endedAt = it.room.endedAt,
                )
            },
            endedRoomCount = all.count { it.room.status == RoomStatus.ENDED },
        )
    }

    @Transactional(readOnly = true)
    fun detail(roomId: String, userId: String): RoomDetailResponse {
        val detail = roomQueryService.detail(roomId, userId)
        val room = detail.room
        val nextOwner = detail.nextOwnerUserId?.let { id -> userQueryService.summaries(listOf(id))[id] }
        val record = verificationQueryService.recordOf(roomId, userId)
        return RoomDetailResponse(
            roomId = room.roomId,
            name = room.name,
            imageUrl = null,
            inviteCode = room.inviteCode.takeUnless { room.status == RoomStatus.ENDED },
            status = room.status,
            endedAt = room.endedAt,
            memberCount = room.memberCount,
            capacity = room.capacity,
            myRole = detail.myRole,
            nextOwner = nextOwner?.let { UserRef(it.userId, it.nickname) },
            myRecord = RoomDetailResponse.MyRecord(
                promiseCount = promiseQueryService.activeCounts(listOf(roomId)).of(roomId, userId),
                verificationCount = record.verificationCount,
                receivedCheerCount = record.receivedCheerCount,
            ),
        )
    }

    @Transactional(readOnly = true)
    fun members(roomId: String, userId: String): MembersResponse {
        val members = roomQueryService.members(roomId, userId)
        val users = userQueryService.summaries(members.map { it.userId })
        val counts = promiseQueryService.activeCounts(listOf(roomId))
        return MembersResponse(
            members = members.mapNotNull { member ->
                val user = users[member.userId] ?: return@mapNotNull null
                MemberResponse(
                    userId = member.userId,
                    nickname = user.nickname,
                    profileImageUrl = null,
                    role = member.role,
                    isMe = member.userId == userId,
                    joinedAt = member.joinedAt,
                    promiseCount = counts.of(roomId, member.userId),
                )
            },
        )
    }

    @Transactional(readOnly = true)
    fun preview(code: String, userId: String): InvitePreviewResponse {
        val preview = roomQueryService.preview(code, userId)
        val users = userQueryService.summaries(preview.previewMemberIds + preview.room.ownerUserId)
        return InvitePreviewResponse(
            joinStatus = preview.joinStatus,
            room = InvitePreviewResponse.PreviewRoom(
                roomId = preview.room.roomId,
                name = preview.room.name,
                imageUrl = null,
                memberCount = preview.room.memberCount,
                capacity = preview.room.capacity,
                ownerNickname = users[preview.room.ownerUserId]?.nickname,
                memberPreview = preview.previewMemberIds.mapNotNull { users[it] }
                    .map { InvitePreviewResponse.PreviewMember(it.nickname, null) },
            ),
        )
    }

    /**
     * 멤버 → 공약 → 인증·항목·응원을 같은 시각으로 한 트랜잭션에서 지운다.
     * 중간에 서버가 죽으면 전부 롤백된다. 한 사람의 한 모임 기록은 많아야 수백 행이라 트랜잭션 하나로 충분하다.
     */
    @Transactional
    fun leave(roomId: String, userId: String): LeaveRoomResponse {
        val now = clock.now()
        val result = roomCommandService.leave(roomId, userId, now)
        deleteRecords(roomId, userId, now)
        return LeaveRoomResponse(result.roomDeleted, result.newOwnerUserId)
    }

    /** 삭제 범위는 나가기와 같은 메서드를 써서 두 경로가 어긋나지 않게 한다. 차단 기록만 더 남는다 */
    @Transactional
    fun kick(roomId: String, ownerUserId: String, targetUserId: String) {
        val now = clock.now()
        roomCommandService.kick(roomId, ownerUserId, targetUserId, now)
        deleteRecords(roomId, targetUserId, now)
    }

    /** 모든 멤버의 진행 중 공약을 "그 멤버 시간대의 오늘"로 끝낸다 */
    @Transactional
    fun end(roomId: String, userId: String): EndRoomResponse {
        val now = clock.now()
        val (room, memberIds) = roomCommandService.end(roomId, userId, now)
        userQueryService.summaries(memberIds).values.forEach { member ->
            promiseCommandService.completeAllOf(roomId, member.userId, clock.today(member.timezone), now)
        }
        return EndRoomResponse(room.status, room.endedAt, finalAverageRate = null)
    }

    private fun deleteRecords(roomId: String, userId: String, now: Instant) {
        val promiseIds = promiseCommandService.deleteAllOf(roomId, userId, now)
        verificationCommandService.deleteAllOf(roomId, userId, promiseIds, now)
    }

    companion object {
        // 진행 중: 내가 마지막으로 인증한 모임 순, 인증이 없으면 참여한 순
        private val ACTIVE_ORDER: Comparator<MyRoomResult> =
            compareByDescending<MyRoomResult, Instant?>(nullsFirst()) { it.lastVerifiedAt }.thenBy { it.joinedAt }

        // 종료: 종료일 최신순
        private val ENDED_ORDER: Comparator<MyRoomResult> =
            compareByDescending<MyRoomResult, Instant?>(nullsFirst()) { it.room.endedAt }
    }
}
