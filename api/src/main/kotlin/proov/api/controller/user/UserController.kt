package proov.api.controller.user

import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import proov.api.common.ratelimit.RateLimiter
import proov.api.common.resolver.CurrentUserId
import proov.api.common.response.ResponseFactory
import proov.api.common.response.SuccessResponse
import proov.api.controller.auth.dto.TokenResponse
import proov.api.controller.user.dto.MeResponse
import proov.api.controller.user.dto.NicknameAvailabilityResponse
import proov.api.controller.user.dto.SignupRequest
import proov.domain.auth.facade.AuthFacade
import proov.domain.user.service.UserQueryService

@RestController
@RequestMapping("/api/v1")
class UserController(
    private val authFacade: AuthFacade,
    private val userQueryService: UserQueryService,
    private val rateLimiter: RateLimiter,
) : UserApi {

    @PostMapping("/users")
    override fun signup(@RequestBody request: SignupRequest): ResponseEntity<SuccessResponse<TokenResponse>> =
        ResponseFactory.success(UserSuccessCode.SIGNUP, TokenResponse.from(authFacade.signup(request.toCommand())))

    @GetMapping("/users/me")
    override fun me(@CurrentUserId userId: String): ResponseEntity<SuccessResponse<MeResponse>> =
        ResponseFactory.success(UserSuccessCode.GET_ME, MeResponse.from(userQueryService.getById(userId)))

    @GetMapping("/nicknames/availability")
    override fun checkNickname(
        @RequestParam value: String,
        request: HttpServletRequest,
    ): ResponseEntity<SuccessResponse<NicknameAvailabilityResponse>> {
        rateLimiter.check("nickname:${request.remoteAddr}", NICKNAME_CHECK_PER_MINUTE)
        return ResponseFactory.success(UserSuccessCode.CHECK_NICKNAME, NicknameAvailabilityResponse.from(userQueryService.checkNickname(value)))
    }

    companion object {
        private const val NICKNAME_CHECK_PER_MINUTE = 30
    }
}
