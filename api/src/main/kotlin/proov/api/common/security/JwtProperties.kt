package proov.api.common.security

import jakarta.validation.constraints.AssertTrue
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated
import java.time.Duration

@Validated
@ConfigurationProperties(prefix = "proov.jwt")
data class JwtProperties(
    @field:NotBlank
    val issuer: String,
    @field:Size(min = 32)
    val accessKey: String,
    @field:Size(min = 32)
    val signupKey: String,
    val accessTtl: Duration = Duration.ofMinutes(30),
    val signupTtl: Duration = Duration.ofMinutes(10),
    val refreshTtl: Duration = Duration.ofDays(30),
    val refreshReuseGrace: Duration = Duration.ofSeconds(10),
    val clockSkew: Duration = Duration.ofSeconds(30),
) {
    @get:AssertTrue(message = "access-key와 signup-key는 달라야 합니다.")
    val hasDistinctKeys: Boolean get() = accessKey != signupKey
}
