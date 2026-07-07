package kindl.global.auth.oauth

import jakarta.validation.Valid
import kindl.global.response.ApiResponse
import kindl.global.response.SuccessResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/auth")
class OauthAuthController(
    private val oauthAuthFacade: OauthAuthFacade,
) {
    @PostMapping("/login/{provider}")
    fun login(
        @PathVariable provider: String,
        @Valid @RequestBody loginRequest: OauthLoginRequest,
    ): ResponseEntity<out ApiResponse> =
        when (val oauthLoginResult = oauthAuthFacade.login(OauthProvider.from(provider), loginRequest.idToken)) {
            is OauthLoginResult.LoggedIn -> SuccessResponse.of(oauthLoginResult.tokens)
            is OauthLoginResult.NeedsSignup -> SuccessResponse.of(
                data = RegistrationResponse.of(oauthLoginResult.registrationKey),
                status = HttpStatus.ACCEPTED,
                code = "SIGNUP_REQUIRED",
                message = "회원가입이 필요합니다.",
            )
        }

    @PostMapping("/signup")
    fun signup(
        @Valid @RequestBody signupRequest: OauthSignupRequest,
    ) = SuccessResponse.of(
        data = oauthAuthFacade.signup(signupRequest.registrationKey, signupRequest.nickname),
        status = HttpStatus.CREATED,
        code = "CREATED",
        message = "회원가입이 완료되었습니다.",
    )
}
