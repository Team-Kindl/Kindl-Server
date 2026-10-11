package proov.api.controller.auth

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import proov.api.common.resolver.CurrentUserId
import proov.api.common.response.ResponseFactory
import proov.api.common.response.SuccessResponse
import proov.api.controller.auth.dto.LoginRequest
import proov.api.controller.auth.dto.LoginResponse
import proov.api.controller.auth.dto.RefreshRequest
import proov.api.controller.auth.dto.TokenResponse
import proov.core.exception.ProovException
import proov.core.extension.toEnumIgnoreCaseOrNull
import proov.core.type.SocialProvider
import proov.domain.auth.exception.AuthFailure
import proov.domain.auth.facade.AuthFacade

@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val authFacade: AuthFacade,
) : AuthApi {

    @PostMapping("/{provider}/login")
    override fun login(
        @PathVariable provider: String,
        @RequestBody request: LoginRequest,
    ): ResponseEntity<SuccessResponse<LoginResponse>> {
        val socialProvider = provider.toEnumIgnoreCaseOrNull<SocialProvider>()
            ?: throw ProovException(AuthFailure.SOCIAL_PROVIDER_UNSUPPORTED)
        val result = authFacade.login(socialProvider, request.idToken!!, request.device!!.toCommand())
        return ResponseFactory.success(AuthSuccessCode.LOGIN, LoginResponse.from(result))
    }

    @PostMapping("/refresh")
    override fun refresh(@RequestBody request: RefreshRequest): ResponseEntity<SuccessResponse<TokenResponse>> =
        ResponseFactory.success(AuthSuccessCode.REFRESH, TokenResponse.from(authFacade.refresh(request.refreshToken!!)))

    @PostMapping("/logout")
    override fun logout(
        @CurrentUserId userId: String,
        @RequestBody request: RefreshRequest,
    ): ResponseEntity<SuccessResponse<Unit>> {
        authFacade.logout(userId, request.refreshToken!!)
        return ResponseFactory.success(AuthSuccessCode.LOGOUT)
    }
}
