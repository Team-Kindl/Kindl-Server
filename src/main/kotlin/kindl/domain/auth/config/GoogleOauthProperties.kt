package kindl.domain.auth.config

import jakarta.validation.constraints.AssertTrue
import jakarta.validation.constraints.NotEmpty
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated

@ConfigurationProperties(prefix = "oauth.google")
@Validated
data class GoogleOauthProperties(
    // 플랫폼(웹/안드/iOS)별로 client ID가 여러 개일 수 있어 리스트로 허용한다.
    @field:NotEmpty
    val clientIds: List<String>,
) {
    @get:AssertTrue(message = "Google client ID는 공백일 수 없습니다.")
    val hasOnlyNonBlankClientIds: Boolean
        get() = clientIds.all { it.isNotBlank() }
}
