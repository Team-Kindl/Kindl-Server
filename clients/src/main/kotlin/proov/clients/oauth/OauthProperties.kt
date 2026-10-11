package proov.clients.oauth

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties(prefix = "proov.oauth")
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
