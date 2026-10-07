package kindl.api.auth.dto.request

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

/** iOS·Android 공통. idToken은 Kakao·Google·Apple의 OIDC ID 토큰이다. */
data class LoginRequest(
    @field:NotBlank @field:Size(max = 16_384)
    val idToken: String?,
    @field:NotNull @field:Valid
    val device: DeviceRequest?,
)
