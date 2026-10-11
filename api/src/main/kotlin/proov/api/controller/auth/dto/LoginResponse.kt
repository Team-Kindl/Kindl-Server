package proov.api.controller.auth.dto

import com.fasterxml.jackson.annotation.JsonInclude
import proov.domain.auth.LoginResult

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
