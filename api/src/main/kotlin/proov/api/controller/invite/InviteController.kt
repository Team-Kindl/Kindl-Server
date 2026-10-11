package proov.api.controller.invite

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import proov.api.common.ratelimit.RateLimiter
import proov.api.common.resolver.CurrentUserId
import proov.api.common.response.ResponseFactory
import proov.api.common.response.SuccessResponse
import proov.api.controller.invite.dto.InvitePreviewResponse
import proov.api.controller.invite.dto.JoinRoomResponse
import proov.domain.room.facade.RoomFacade
import proov.domain.room.service.RoomService
import java.time.Clock

@RestController
@RequestMapping("/api/v1/invites")
class InviteController(
    private val roomFacade: RoomFacade,
    private val roomService: RoomService,
    private val rateLimiter: RateLimiter,
    private val clock: Clock,
) : InviteApi {

    @GetMapping("/{code}")
    override fun preview(
        @CurrentUserId userId: String,
        @PathVariable code: String,
    ): ResponseEntity<SuccessResponse<InvitePreviewResponse>> {
        rateLimiter.check(rateKey(userId), LIMIT_PER_MINUTE)
        return ResponseFactory.success(InviteSuccessCode.PREVIEW_INVITE, InvitePreviewResponse.from(roomFacade.preview(code, userId)))
    }

    @PostMapping("/{code}/join")
    override fun join(
        @CurrentUserId userId: String,
        @PathVariable code: String,
    ): ResponseEntity<SuccessResponse<JoinRoomResponse>> {
        rateLimiter.check(rateKey(userId), LIMIT_PER_MINUTE)
        return ResponseFactory.success(InviteSuccessCode.JOIN_ROOM, JoinRoomResponse.from(roomService.join(code, userId, clock.instant())))
    }

    private fun rateKey(userId: String) = "invite:$userId"

    companion object {
        private const val LIMIT_PER_MINUTE = 20
    }
}
