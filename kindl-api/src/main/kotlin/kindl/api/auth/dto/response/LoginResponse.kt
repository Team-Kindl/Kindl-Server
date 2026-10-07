package kindl.api.auth.dto.response

import com.fasterxml.jackson.annotation.JsonInclude
import kindl.api.auth.dto.result.LoginResult

/** 앱은 status 하나로 다음 화면을 정한다: SIGNED_IN → 홈, SIGNUP_REQUIRED → 익명 프로필 만들기. */
@JsonInclude(JsonInclude.Include.NON_NULL)
data class LoginResponse(
    val status: Status,
    val tokens: TokenResponse? = null,
    val signupToken: String? = null,
) {
    enum class Status { SIGNED_IN, SIGNUP_REQUIRED }

    companion object {
        fun from(result: LoginResult) = when (result) {
            is LoginResult.SignedIn -> LoginResponse(Status.SIGNED_IN, tokens = TokenResponse.from(result.tokens))
            is LoginResult.SignupRequired -> LoginResponse(Status.SIGNUP_REQUIRED, signupToken = result.signupToken)
        }
    }
}
