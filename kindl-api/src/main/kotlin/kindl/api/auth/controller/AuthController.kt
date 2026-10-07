package kindl.api.auth.controller

import jakarta.validation.Valid
import kindl.api.auth.dto.request.LoginRequest
import kindl.api.auth.dto.request.RefreshRequest
import kindl.api.auth.dto.response.LoginResponse
import kindl.api.auth.dto.response.TokenResponse
import kindl.api.auth.facade.AuthFacade
import kindl.api.common.response.SuccessResponse
import kindl.api.security.CurrentUser
import kindl.core.extension.orThrow
import kindl.core.extension.toEnumIgnoreCaseOrNull
import kindl.core.type.SocialProvider
import kindl.domain.auth.error.AuthError
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val authFacade: AuthFacade,
) {
    /** 기존 유저면 토큰, 처음이면 10분짜리 signupToken. */
    @PostMapping("/{provider}/login")
    fun login(
        @PathVariable provider: String,
        @Valid @RequestBody request: LoginRequest,
    ): ResponseEntity<SuccessResponse<LoginResponse>> {
        // 모르는 provider는 형식 오류(INVALID_INPUT)가 아니라 앱이 분기할 수 있는 전용 코드로 돌려준다
        val socialProvider = provider.toEnumIgnoreCaseOrNull<SocialProvider>()
            .orThrow(AuthError.SOCIAL_PROVIDER_UNSUPPORTED)
        val result = authFacade.login(socialProvider, request.idToken!!, request.device!!.toCommand())
        return SuccessResponse.of(LoginResponse.from(result))
    }

    @PostMapping("/refresh")
    fun refresh(@Valid @RequestBody request: RefreshRequest): ResponseEntity<SuccessResponse<TokenResponse>> =
        SuccessResponse.of(TokenResponse.from(authFacade.refresh(request.refreshToken!!)))

    /** 이 기기의 refresh token 묶음을 폐기한다. access token은 만료까지 남는다(최대 30분). */
    @PostMapping("/logout")
    fun logout(
        @CurrentUser userId: String,
        @Valid @RequestBody request: RefreshRequest,
    ): ResponseEntity<SuccessResponse<Unit>> {
        authFacade.logout(userId, request.refreshToken!!)
        return SuccessResponse.of(null)
    }
}
