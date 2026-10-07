package kindl.api.room.controller

import kindl.api.common.ratelimit.RateLimiter
import kindl.api.common.response.SuccessResponse
import kindl.api.room.dto.response.InvitePreviewResponse
import kindl.api.room.dto.response.JoinRoomResponse
import kindl.api.room.facade.RoomFacade
import kindl.api.security.CurrentUser
import kindl.core.time.ServiceClock
import kindl.domain.room.service.RoomService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/invites")
class InviteController(
    private val roomFacade: RoomFacade,
    private val roomService: RoomService,
    private val rateLimiter: RateLimiter,
    private val clock: ServiceClock,
) {
    @GetMapping("/{code}")
    fun preview(
        @CurrentUser userId: String,
        @PathVariable code: String,
    ): ResponseEntity<SuccessResponse<InvitePreviewResponse>> {
        rateLimiter.check(rateKey(userId), LIMIT_PER_MINUTE)
        return SuccessResponse.of(roomFacade.preview(code, userId))
    }

    /** 모임 하나만 다루므로 Facade를 거치지 않는다 */
    @PostMapping("/{code}/join")
    fun join(
        @CurrentUser userId: String,
        @PathVariable code: String,
    ): ResponseEntity<SuccessResponse<JoinRoomResponse>> {
        rateLimiter.check(rateKey(userId), LIMIT_PER_MINUTE)
        val room = roomService.join(code, userId, clock.now())
        return SuccessResponse.of(JoinRoomResponse(room.roomId, room.name))
    }

    private fun rateKey(userId: String) = "invite:$userId"

    companion object {
        // 미리보기·입장 합산. 사람이 코드를 고쳐 입력하기엔 넉넉하고, 무작위 대입에는 의미 없는 횟수
        private const val LIMIT_PER_MINUTE = 20
    }
}
