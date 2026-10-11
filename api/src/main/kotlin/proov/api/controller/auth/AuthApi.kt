package proov.api.controller.auth

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import proov.api.common.response.SuccessResponse
import proov.api.controller.auth.dto.LoginRequest
import proov.api.controller.auth.dto.LoginResponse
import proov.api.controller.auth.dto.RefreshRequest
import proov.api.controller.auth.dto.TokenResponse

@Tag(name = "Auth", description = "소셜 로그인과 토큰")
interface AuthApi {

    @Operation(summary = "소셜 로그인", description = "가입된 계정이면 토큰을, 처음이면 signupToken을 준다.")
    fun login(
        @Parameter(description = "kakao · google · apple") provider: String,
        @Valid request: LoginRequest,
    ): ResponseEntity<SuccessResponse<LoginResponse>>

    @Operation(summary = "토큰 재발급", description = "refresh token을 회전한다.")
    fun refresh(@Valid request: RefreshRequest): ResponseEntity<SuccessResponse<TokenResponse>>

    @Operation(summary = "로그아웃", description = "이 기기의 refresh token을 폐기한다.")
    fun logout(
        @Parameter(hidden = true) userId: String,
        @Valid request: RefreshRequest,
    ): ResponseEntity<SuccessResponse<Unit>>
}
