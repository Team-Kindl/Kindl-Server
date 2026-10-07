package kindl.api.room.controller

import jakarta.validation.Valid
import java.util.UUID
import kindl.api.common.response.SuccessResponse
import kindl.api.room.dto.request.CreateRoomRequest
import kindl.api.room.dto.response.EndRoomResponse
import kindl.api.room.dto.response.LeaveRoomResponse
import kindl.api.room.dto.response.MembersResponse
import kindl.api.room.dto.response.MyRoomsResponse
import kindl.api.room.dto.response.RoomDetailResponse
import kindl.api.room.dto.response.RoomResponse
import kindl.api.room.facade.RoomFacade
import kindl.api.security.CurrentUser
import kindl.core.time.ServiceClock
import kindl.domain.room.enums.RoomStatus
import kindl.domain.room.service.RoomCommandService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/** 모임 하나만 다루는 요청(만들기)은 서비스를, 여러 도메인을 엮는 요청은 Facade를 부른다 */
@RestController
@RequestMapping("/api/v1/rooms")
class RoomController(
    private val roomCommandService: RoomCommandService,
    private val roomFacade: RoomFacade,
    private val clock: ServiceClock,
) {
    @PostMapping
    fun create(
        @CurrentUser userId: String,
        @RequestHeader(IDEMPOTENCY_KEY) idempotencyKey: UUID,
        @Valid @RequestBody request: CreateRoomRequest,
    ): ResponseEntity<SuccessResponse<RoomResponse>> {
        val room = roomCommandService.create(request.toCommand(userId, idempotencyKey), clock.now())
        return SuccessResponse.of(RoomResponse.from(room), HttpStatus.CREATED, "CREATED")
    }

    @GetMapping
    fun myRooms(
        @CurrentUser userId: String,
        @RequestParam(defaultValue = "ACTIVE") status: RoomStatus,
    ): ResponseEntity<SuccessResponse<MyRoomsResponse>> =
        SuccessResponse.of(roomFacade.myRooms(userId, status))

    @GetMapping("/{roomId}")
    fun detail(
        @CurrentUser userId: String,
        @PathVariable roomId: String,
    ): ResponseEntity<SuccessResponse<RoomDetailResponse>> =
        SuccessResponse.of(roomFacade.detail(roomId, userId))

    @GetMapping("/{roomId}/members")
    fun members(
        @CurrentUser userId: String,
        @PathVariable roomId: String,
    ): ResponseEntity<SuccessResponse<MembersResponse>> =
        SuccessResponse.of(roomFacade.members(roomId, userId))

    @DeleteMapping("/{roomId}/members/me")
    fun leave(
        @CurrentUser userId: String,
        @PathVariable roomId: String,
    ): ResponseEntity<SuccessResponse<LeaveRoomResponse>> =
        SuccessResponse.of(roomFacade.leave(roomId, userId))

    @DeleteMapping("/{roomId}/members/{targetUserId}")
    fun kick(
        @CurrentUser userId: String,
        @PathVariable roomId: String,
        @PathVariable targetUserId: String,
    ): ResponseEntity<SuccessResponse<Unit>> {
        roomFacade.kick(roomId, userId, targetUserId)
        return SuccessResponse.of(null)
    }

    @PostMapping("/{roomId}/end")
    fun end(
        @CurrentUser userId: String,
        @PathVariable roomId: String,
    ): ResponseEntity<SuccessResponse<EndRoomResponse>> =
        SuccessResponse.of(roomFacade.end(roomId, userId))

    companion object {
        const val IDEMPOTENCY_KEY = "Idempotency-Key"
    }
}
