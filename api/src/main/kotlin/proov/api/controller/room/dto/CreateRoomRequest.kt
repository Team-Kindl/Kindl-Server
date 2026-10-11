package proov.api.controller.room.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class CreateRoomRequest(
    @field:NotBlank @field:Size(max = 60)
    val name: String?,
)
