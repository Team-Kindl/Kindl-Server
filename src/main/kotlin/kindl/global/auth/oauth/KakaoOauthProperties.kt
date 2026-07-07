package kindl.global.auth.oauth

import jakarta.validation.constraints.AssertTrue
import jakarta.validation.constraints.NotEmpty
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated

@ConfigurationProperties(prefix = "oauth.kakao")
@Validated
data class KakaoOauthProperties(
    @field:NotEmpty
    val clientIds: List<String>,
) {
    @get:AssertTrue(message = "Kakao client ID는 공백일 수 없습니다.")
    val hasOnlyNonBlankClientIds: Boolean
        get() = clientIds.all { it.isNotBlank() }
}
