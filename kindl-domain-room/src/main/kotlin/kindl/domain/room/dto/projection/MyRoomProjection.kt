package kindl.domain.room.dto.projection

import kindl.domain.room.enums.MemberRole
import kindl.domain.room.enums.RoomStatus
import java.time.Instant

/** 내 모임 목록 한 줄: 모임 컬럼 + 그 모임에서의 내 멤버십 컬럼 */
data class MyRoomProjection(
    val roomId: String,
    val name: String,
    val imageFileId: String?,
    val inviteCode: String,
    val ownerUserId: String,
    val status: RoomStatus,
    val memberCount: Int,
    val endedAt: Instant?,
    val myRole: MemberRole,
    val joinedAt: Instant,
    val lastVerifiedAt: Instant?,
)
