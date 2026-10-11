package proov.domain.room.facade

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import proov.domain.room.InvitePreview
import proov.domain.room.RoomDetail
import proov.domain.room.RoomMember
import proov.domain.room.service.RoomService
import proov.domain.user.User
import proov.domain.user.port.RoomUserPort

@Component
class RoomFacade(
    private val roomService: RoomService,
    private val roomUserPort: RoomUserPort,
) {
    @Transactional(readOnly = true)
    fun detail(roomId: String, userId: String): RoomDetailResult {
        val detail = roomService.detail(roomId, userId)
        val nextOwner = detail.nextOwnerUserId?.let { roomUserPort.findAllByIds(listOf(it)).firstOrNull() }
        return RoomDetailResult(detail, nextOwner)
    }

    @Transactional(readOnly = true)
    fun members(roomId: String, userId: String): List<MemberProfile> {
        val members = roomService.members(roomId, userId)
        val users = roomUserPort.findAllByIds(members.map { it.userId }).associateBy { it.id }
        return members.mapNotNull { member -> users[member.userId]?.let { MemberProfile(member, it) } }
    }

    @Transactional(readOnly = true)
    fun preview(code: String, userId: String): InvitePreviewResult {
        val preview = roomService.preview(code, userId)
        val users = roomUserPort.findAllByIds(preview.previewMemberIds + preview.room.ownerUserId).associateBy { it.id }
        return InvitePreviewResult(preview, users[preview.room.ownerUserId], preview.previewMemberIds.mapNotNull { users[it] })
    }
}

data class RoomDetailResult(
    val detail: RoomDetail,
    val nextOwner: User?,
)

data class MemberProfile(
    val member: RoomMember,
    val user: User,
)

data class InvitePreviewResult(
    val preview: InvitePreview,
    val owner: User?,
    val previewMembers: List<User>,
)
