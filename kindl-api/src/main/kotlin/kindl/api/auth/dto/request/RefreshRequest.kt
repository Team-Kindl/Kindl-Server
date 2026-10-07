package kindl.api.auth.dto.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

// 헤더·쿼리에 두면 접근 로그·프록시 로그에 남기 쉬워 본문으로 받는다
data class RefreshRequest(
    @field:NotBlank @field:Size(max = 128)
    val refreshToken: String?,
)
