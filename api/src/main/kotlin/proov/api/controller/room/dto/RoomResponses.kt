package proov.api.controller.room.dto

import proov.domain.room.MyRoom
import proov.domain.room.Room
import proov.domain.room.enums.MemberRole
import proov.domain.room.enums.RoomStatus
import proov.domain.room.facade.MemberProfile
import proov.domain.room.facade.RoomDetailResult
import java.time.Instant

data class RoomResponse(
    val roomId: String,
    val name: String,
    val inviteCode: String,
    val memberCount: Int,
    val capacity: Int,
) {
    companion object {
        fun from(room: Room) = RoomResponse(room.id, room.name.value, room.inviteCode.value, room.memberCount, Room.CAPACITY)
    }
}

data class MyRoomsResponse(
    val rooms: List<RoomCardResponse>,
    val endedRoomCount: Int,
)

data class RoomCardResponse(
    val roomId: String,
    val name: String,
    val memberCount: Int,
    val capacity: Int,
    val endedAt: Instant?,
) {
    companion object {
        fun from(myRoom: MyRoom) = with(myRoom.room) { RoomCardResponse(id, name.value, memberCount, Room.CAPACITY, endedAt) }
    }
}

data class RoomDetailResponse(
    val roomId: String,
    val name: String,
    val inviteCode: String?,
    val status: RoomStatus,
    val endedAt: Instant?,
    val memberCount: Int,
    val capacity: Int,
    val myRole: MemberRole,
    val nextOwner: UserRef?,
) {
    companion object {
        fun from(result: RoomDetailResult) = with(result.detail.room) {
            RoomDetailResponse(
                roomId = id,
                name = name.value,
                inviteCode = inviteCode.value.takeUnless { isEnded },
                status = status,
                endedAt = endedAt,
                memberCount = memberCount,
                capacity = Room.CAPACITY,
                myRole = result.detail.myRole,
                nextOwner = result.nextOwner?.let { UserRef(it.id, it.nickname.value) },
            )
        }
    }
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
    val role: MemberRole,
    val isMe: Boolean,
    val joinedAt: Instant,
) {
    companion object {
        fun from(profile: MemberProfile, myUserId: String) = MemberResponse(
            userId = profile.member.userId,
            nickname = profile.user.nickname.value,
            role = profile.member.role,
            isMe = profile.member.userId == myUserId,
            joinedAt = profile.member.joinedAt,
        )
    }
}

data class LeaveRoomResponse(
    val roomDeleted: Boolean,
    val newOwnerUserId: String?,
)

data class EndRoomResponse(
    val status: RoomStatus,
    val endedAt: Instant?,
)
