package proov.api.controller.invite

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import proov.api.common.response.SuccessResponse
import proov.api.controller.invite.dto.InvitePreviewResponse
import proov.api.controller.invite.dto.JoinRoomResponse

@Tag(name = "Invite", description = "초대 코드")
interface InviteApi {

    @Operation(summary = "초대 미리보기", description = "들어갈 수 없는 경우도 joinStatus로 알려 준다.")
    fun preview(@Parameter(hidden = true) userId: String, code: String): ResponseEntity<SuccessResponse<InvitePreviewResponse>>

    @Operation(summary = "초대 코드로 입장")
    fun join(@Parameter(hidden = true) userId: String, code: String): ResponseEntity<SuccessResponse<JoinRoomResponse>>
}
