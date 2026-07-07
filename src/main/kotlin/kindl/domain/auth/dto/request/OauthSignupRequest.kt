package kindl.domain.auth.dto.request

import jakarta.validation.constraints.AssertTrue
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import kindl.domain.user.entity.User

data class OauthSignupRequest(
    @field:NotBlank
    @field:Pattern(
        regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[1-5][0-9a-fA-F]{3}-[89aAbB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}$",
        message = "가입 키 형식이 올바르지 않습니다.",
    )
    val registrationKey: String,
    @field:NotBlank
    @field:Size(max = User.MAX_NICKNAME_LENGTH)
    val nickname: String,
    @field:AssertTrue(message = "약관에 동의해야 합니다.")
    val termsAgreed: Boolean,
)
