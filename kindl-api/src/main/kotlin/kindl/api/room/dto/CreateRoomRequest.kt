package kindl.api.room.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import kindl.domain.room.command.CreateRoomCommand
import kindl.domain.room.vo.RoomName
import java.util.UUID

/** 형식(필수·상한)은 여기서, 이름 규칙(그래핌 15자·이모지)은 RoomName이 맡는다 */
data class CreateRoomRequest(
    @field:NotBlank @field:Size(max = 60)
    val name: String?,
) {
    fun toCommand(userId: String, idempotencyKey: UUID) = CreateRoomCommand(
        userId = userId,
        name = RoomName.of(name!!),
        clientRequestId = idempotencyKey.toString(),
    )
}
