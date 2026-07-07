package kindl.global.auth.jwt

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated

@ConfigurationProperties(prefix = "jwt")
@Validated
data class JwtProperties(
    @field:NotBlank
    @field:Size(min = 32)
    val secret: String,
    @field:Positive
    val accessTokenExpireTime: Long,
    @field:Positive
    val refreshTokenExpireTime: Long,
)
