package proov.api.controller.room

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import proov.api.common.response.SuccessResponse
import proov.api.common.validation.ValidEnum
import proov.api.controller.room.dto.CreateRoomRequest
import proov.api.controller.room.dto.EndRoomResponse
import proov.api.controller.room.dto.LeaveRoomResponse
import proov.api.controller.room.dto.MembersResponse
import proov.api.controller.room.dto.MyRoomsResponse
import proov.api.controller.room.dto.RoomDetailResponse
import proov.api.controller.room.dto.RoomResponse
import proov.domain.room.enums.RoomStatus
import java.util.UUID

@Tag(name = "Room", description = "모임")
interface RoomApi {

    @Operation(summary = "모임 만들기", description = "같은 Idempotency-Key로 다시 요청하면 처음 만든 모임을 돌려준다.")
    fun create(
        @Parameter(hidden = true) userId: String,
        idempotencyKey: UUID,
        @Valid request: CreateRoomRequest,
    ): ResponseEntity<SuccessResponse<RoomResponse>>

    @Operation(summary = "내 모임 목록")
    fun myRooms(
        @Parameter(hidden = true) userId: String,
        @Parameter(description = "ACTIVE · ENDED") @ValidEnum(RoomStatus::class) status: String,
    ): ResponseEntity<SuccessResponse<MyRoomsResponse>>

    @Operation(summary = "모임 상세")
    fun detail(@Parameter(hidden = true) userId: String, roomId: String): ResponseEntity<SuccessResponse<RoomDetailResponse>>

    @Operation(summary = "모임 멤버 목록")
    fun members(@Parameter(hidden = true) userId: String, roomId: String): ResponseEntity<SuccessResponse<MembersResponse>>

    @Operation(summary = "모임 나가기", description = "모임장이 나가면 가장 먼저 들어온 멤버가 모임장이 되고, 혼자면 모임이 사라진다.")
    fun leave(@Parameter(hidden = true) userId: String, roomId: String): ResponseEntity<SuccessResponse<LeaveRoomResponse>>

    @Operation(summary = "멤버 내보내기", description = "모임장만 할 수 있고, 내보낸 사람은 다시 들어올 수 없다.")
    fun kick(
        @Parameter(hidden = true) userId: String,
        roomId: String,
        targetUserId: String,
    ): ResponseEntity<SuccessResponse<Unit>>

    @Operation(summary = "모임 종료", description = "모임장만 할 수 있다.")
    fun end(@Parameter(hidden = true) userId: String, roomId: String): ResponseEntity<SuccessResponse<EndRoomResponse>>
}
