package kindl.api.user.controller

import jakarta.validation.Valid
import jakarta.validation.constraints.Size
import kindl.api.auth.dto.response.TokenResponse
import kindl.api.auth.facade.AuthFacade
import kindl.api.common.response.SuccessResponse
import kindl.api.security.CurrentUser
import kindl.api.user.dto.request.SignupRequest
import kindl.api.user.dto.response.MeResponse
import kindl.api.user.dto.response.NicknameAvailabilityResponse
import kindl.domain.user.service.UserQueryService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1")
class UserController(
    private val authFacade: AuthFacade,
    private val userQueryService: UserQueryService,
) {
    /** signupToken + 익명 프로필로 유저를 만들고 바로 토큰을 준다. */
    @PostMapping("/users")
    fun signup(@Valid @RequestBody request: SignupRequest): ResponseEntity<SuccessResponse<TokenResponse>> =
        SuccessResponse.of(
            data = TokenResponse.from(authFacade.signup(request.toCommand())),
            status = HttpStatus.CREATED,
            code = "CREATED",
            message = "회원가입이 완료되었습니다.",
        )

    @GetMapping("/users/me")
    fun me(@CurrentUser userId: String): ResponseEntity<SuccessResponse<MeResponse>> =
        SuccessResponse.of(MeResponse.from(userQueryService.getActive(userId)))

    /** 입력 중 확인용. 최종 판정은 가입 요청의 유니크 인덱스다. */
    @GetMapping("/nicknames/availability")
    fun checkNickname(
        @RequestParam @Size(max = 60) value: String,
    ): ResponseEntity<SuccessResponse<NicknameAvailabilityResponse>> =
        SuccessResponse.of(NicknameAvailabilityResponse.from(userQueryService.checkNickname(value)))
}
