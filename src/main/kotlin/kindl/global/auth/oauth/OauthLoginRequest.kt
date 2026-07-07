package kindl.global.auth.oauth

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class OauthLoginRequest(
    @field:NotBlank
    @field:Size(max = 16_384)
    val idToken: String,
)
