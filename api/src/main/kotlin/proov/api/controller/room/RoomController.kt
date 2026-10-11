package proov.api.controller.room

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
import proov.api.common.resolver.CurrentUserId
import proov.api.common.response.ResponseFactory
import proov.api.common.response.SuccessResponse
import proov.api.controller.room.dto.CreateRoomRequest
import proov.api.controller.room.dto.EndRoomResponse
import proov.api.controller.room.dto.LeaveRoomResponse
import proov.api.controller.room.dto.MemberResponse
import proov.api.controller.room.dto.MembersResponse
import proov.api.controller.room.dto.MyRoomsResponse
import proov.api.controller.room.dto.RoomCardResponse
import proov.api.controller.room.dto.RoomDetailResponse
import proov.api.controller.room.dto.RoomResponse
import proov.core.extension.toEnumIgnoreCase
import proov.domain.room.MyRoom
import proov.domain.room.RoomName
import proov.domain.room.enums.RoomStatus
import proov.domain.room.facade.RoomFacade
import proov.domain.room.service.RoomService
import java.time.Clock
import java.time.Instant
import java.util.UUID

@RestController
@RequestMapping("/api/v1/rooms")
class RoomController(
    private val roomService: RoomService,
    private val roomFacade: RoomFacade,
    private val clock: Clock,
) : RoomApi {

    @PostMapping
    override fun create(
        @CurrentUserId userId: String,
        @RequestHeader(IDEMPOTENCY_KEY) idempotencyKey: UUID,
        @RequestBody request: CreateRoomRequest,
    ): ResponseEntity<SuccessResponse<RoomResponse>> {
        val room = roomService.create(userId, RoomName.of(request.name!!), idempotencyKey.toString(), clock.instant())
        return ResponseFactory.success(RoomSuccessCode.CREATE_ROOM, RoomResponse.from(room))
    }

    @GetMapping
    override fun myRooms(
        @CurrentUserId userId: String,
        @RequestParam(defaultValue = "ACTIVE") status: String,
    ): ResponseEntity<SuccessResponse<MyRoomsResponse>> {
        val roomStatus = status.toEnumIgnoreCase<RoomStatus>()
        val all = roomService.myRooms(userId)
        val rooms = all.filter { it.room.status == roomStatus }
            .sortedWith(if (roomStatus == RoomStatus.ACTIVE) ACTIVE_ORDER else ENDED_ORDER)
            .map(RoomCardResponse::from)
        val response = MyRoomsResponse(rooms, endedRoomCount = all.count { it.room.isEnded })
        return ResponseFactory.success(RoomSuccessCode.GET_MY_ROOMS, response)
    }

    @GetMapping("/{roomId}")
    override fun detail(
        @CurrentUserId userId: String,
        @PathVariable roomId: String,
    ): ResponseEntity<SuccessResponse<RoomDetailResponse>> =
        ResponseFactory.success(RoomSuccessCode.GET_ROOM, RoomDetailResponse.from(roomFacade.detail(roomId, userId)))

    @GetMapping("/{roomId}/members")
    override fun members(
        @CurrentUserId userId: String,
        @PathVariable roomId: String,
    ): ResponseEntity<SuccessResponse<MembersResponse>> {
        val members = roomFacade.members(roomId, userId).map { MemberResponse.from(it, userId) }
        return ResponseFactory.success(RoomSuccessCode.GET_MEMBERS, MembersResponse(members))
    }

    @DeleteMapping("/{roomId}/members/me")
    override fun leave(
        @CurrentUserId userId: String,
        @PathVariable roomId: String,
    ): ResponseEntity<SuccessResponse<LeaveRoomResponse>> {
        val result = roomService.leave(roomId, userId)
        return ResponseFactory.success(RoomSuccessCode.LEAVE_ROOM, LeaveRoomResponse(result.roomDeleted, result.newOwnerUserId))
    }

    @DeleteMapping("/{roomId}/members/{targetUserId}")
    override fun kick(
        @CurrentUserId userId: String,
        @PathVariable roomId: String,
        @PathVariable targetUserId: String,
    ): ResponseEntity<SuccessResponse<Unit>> {
        roomService.kick(roomId, userId, targetUserId)
        return ResponseFactory.success(RoomSuccessCode.KICK_MEMBER)
    }

    @PostMapping("/{roomId}/end")
    override fun end(
        @CurrentUserId userId: String,
        @PathVariable roomId: String,
    ): ResponseEntity<SuccessResponse<EndRoomResponse>> {
        val room = roomService.end(roomId, userId, clock.instant())
        return ResponseFactory.success(RoomSuccessCode.END_ROOM, EndRoomResponse(room.status, room.endedAt))
    }

    companion object {
        const val IDEMPOTENCY_KEY = "Idempotency-Key"

        private val ACTIVE_ORDER: Comparator<MyRoom> = compareBy { it.joinedAt }

        private val ENDED_ORDER: Comparator<MyRoom> = compareByDescending<MyRoom, Instant?>(nullsFirst()) { it.room.endedAt }
    }
}
