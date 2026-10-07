package kindl.infra.social

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

/**
 * 제공자별 client ID 목록. 플랫폼(iOS·Android·웹)마다 다를 수 있어 여러 개를 허용한다.
 * 목록이 빈 제공자는 검증기를 등록하지 않는다 → SOCIAL_PROVIDER_UNSUPPORTED.
 */
@ConfigurationProperties(prefix = "kindl.oauth")
data class OauthProperties(
    val google: Provider = Provider(),
    val kakao: Provider = Provider(),
    val apple: Provider = Provider(),
    val connectTimeout: Duration = Duration.ofSeconds(1),
    val readTimeout: Duration = Duration.ofSeconds(3),
) {
    data class Provider(
        val clientIds: List<String> = emptyList(),
    ) {
        val enabledClientIds: List<String> get() = clientIds.filter { it.isNotBlank() }
    }
}
