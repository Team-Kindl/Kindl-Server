package proov.api.controller.user

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import jakarta.validation.constraints.Size
import org.springframework.http.ResponseEntity
import proov.api.common.response.SuccessResponse
import proov.api.controller.auth.dto.TokenResponse
import proov.api.controller.user.dto.MeResponse
import proov.api.controller.user.dto.NicknameAvailabilityResponse
import proov.api.controller.user.dto.SignupRequest

@Tag(name = "User", description = "회원가입과 내 정보")
interface UserApi {

    @Operation(summary = "회원가입", description = "signupToken과 익명 프로필로 유저를 만들고 토큰을 준다.")
    fun signup(@Valid request: SignupRequest): ResponseEntity<SuccessResponse<TokenResponse>>

    @Operation(summary = "내 정보")
    fun me(@Parameter(hidden = true) userId: String): ResponseEntity<SuccessResponse<MeResponse>>

    @Operation(summary = "닉네임 사용 가능 여부")
    fun checkNickname(@Size(max = 60) value: String): ResponseEntity<SuccessResponse<NicknameAvailabilityResponse>>
}
