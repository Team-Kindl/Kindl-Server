package kindl.api.room.dto

import kindl.domain.room.enums.MemberRole
import kindl.domain.room.enums.RoomStatus
import kindl.domain.room.result.JoinStatus
import kindl.domain.room.result.RoomResult
import java.time.Instant

data class RoomResponse(
    val roomId: String,
    val name: String,
    // 사진 업로드 기능이 붙으면 1시간 유효 서명 URL. 지금은 항상 null (앱은 이름 첫 글자 타일)
    val imageUrl: String?,
    val inviteCode: String,
    val memberCount: Int,
    val capacity: Int,
) {
    companion object {
        fun from(room: RoomResult) = RoomResponse(room.roomId, room.name, null, room.inviteCode, room.memberCount, room.capacity)
    }
}

data class MyRoomsResponse(
    val rooms: List<RoomCardResponse>,
    // "종료된 모임 N개 ›" 카드용
    val endedRoomCount: Int,
)

/** 달성률(roomAverageRate·myRate)은 공약 기능에서 필드를 추가한다 (추가만이라 앱 호환) */
data class RoomCardResponse(
    val roomId: String,
    val name: String,
    val imageUrl: String?,
    val memberCount: Int,
    val capacity: Int,
    val activePromiseCount: Int,
    val myPromiseCount: Int,
    val endedAt: Instant?,
)

data class RoomDetailResponse(
    val roomId: String,
    val name: String,
    val imageUrl: String?,
    // 종료된 모임은 코드가 무효라 null
    val inviteCode: String?,
    val status: RoomStatus,
    val endedAt: Instant?,
    val memberCount: Int,
    val capacity: Int,
    val myRole: MemberRole,
    val nextOwner: UserRef?,
    val myRecord: MyRecord,
) {
    /** 나가기 시트의 "내 공약 N개 · 인증 N건 · 응원 N개" */
    data class MyRecord(
        val promiseCount: Int,
        val verificationCount: Int,
        val receivedCheerCount: Int,
    )
}

data class UserRef(
    val userId: String,
    val nickname: String,
)

data class MembersResponse(
    val members: List<MemberResponse>,
)

data class MemberResponse(
    val userId: String,
    val nickname: String,
    val profileImageUrl: String?,
    val role: MemberRole,
    val isMe: Boolean,
    val joinedAt: Instant,
    val promiseCount: Int,
)

data class InvitePreviewResponse(
    val joinStatus: JoinStatus,
    val room: PreviewRoom,
) {
    data class PreviewRoom(
        val roomId: String,
        val name: String,
        val imageUrl: String?,
        val memberCount: Int,
        val capacity: Int,
        val ownerNickname: String?,
        val memberPreview: List<PreviewMember>,
    )

    data class PreviewMember(
        val nickname: String,
        val profileImageUrl: String?,
    )
}

data class JoinRoomResponse(
    val roomId: String,
    val name: String,
)

data class LeaveRoomResponse(
    val roomDeleted: Boolean,
    val newOwnerUserId: String?,
)

data class EndRoomResponse(
    val status: RoomStatus,
    val endedAt: Instant?,
    // 공약 달성률 계산이 붙으면 종료 시점 평균 스냅샷을 채운다
    val finalAverageRate: Int?,
)
