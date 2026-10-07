package kindl.domain.room.dto.result

import kindl.domain.room.enums.MemberRole
import kindl.domain.room.dto.projection.MyRoomProjection
import kindl.domain.room.entity.Room
import kindl.domain.room.entity.RoomMember
import kindl.domain.room.enums.RoomStatus
import java.time.Instant

data class RoomResult(
    val roomId: String,
    val name: String,
    val imageFileId: String?,
    val inviteCode: String,
    val ownerUserId: String,
    val status: RoomStatus,
    val memberCount: Int,
    val capacity: Int,
    val endedAt: Instant?,
) {
    companion object {
        fun from(room: Room) = RoomResult(
            roomId = requireNotNull(room.id),
            name = room.name,
            imageFileId = room.imageFileId,
            inviteCode = room.inviteCode,
            ownerUserId = room.ownerUserId,
            status = room.status,
            memberCount = room.memberCount,
            capacity = Room.CAPACITY,
            endedAt = room.endedAt,
        )
    }
}

data class MemberResult(
    val userId: String,
    val role: MemberRole,
    val joinedAt: Instant,
) {
    companion object {
        fun from(member: RoomMember) = MemberResult(member.userId, member.role, member.joinedAt)
    }
}

/** 내 모임 목록 한 줄. 정렬 기준(lastVerifiedAt·joinedAt)을 함께 들고 다닌다 */
data class MyRoomResult(
    val room: RoomResult,
    val myRole: MemberRole,
    val joinedAt: Instant,
    val lastVerifiedAt: Instant?,
) {
    companion object {
        fun from(row: MyRoomProjection) = MyRoomResult(
            room = RoomResult(
                roomId = row.roomId,
                name = row.name,
                imageFileId = row.imageFileId,
                inviteCode = row.inviteCode,
                ownerUserId = row.ownerUserId,
                status = row.status,
                memberCount = row.memberCount,
                capacity = Room.CAPACITY,
                endedAt = row.endedAt,
            ),
            myRole = row.myRole,
            joinedAt = row.joinedAt,
            lastVerifiedAt = row.lastVerifiedAt,
        )
    }
}

data class RoomDetailResult(
    val room: RoomResult,
    val myRole: MemberRole,
    // 내가 모임장일 때 나가면 승계받을 사람 (가장 먼저 들어온 다른 멤버)
    val nextOwnerUserId: String?,
)

enum class JoinStatus {
    JOINABLE,
    ALREADY_MEMBER,
    BANNED,
    ROOM_ENDED,
    ROOM_FULL,
    ROOM_LIMIT,
}

data class InvitePreviewResult(
    val joinStatus: JoinStatus,
    val room: RoomResult,
    // 미리보기 얼굴용 멤버 (입장 순, 최대 5명)
    val previewMemberIds: List<String>,
)

data class LeaveResult(
    val roomDeleted: Boolean,
    val newOwnerUserId: String?,
)
