package proov.support

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.testcontainers.postgresql.PostgreSQLContainer
import proov.core.type.SocialProvider
import proov.domain.auth.fixture.FakeSocialTokenVerifier
import proov.domain.auth.port.SocialTokenVerifierPort
import java.time.Instant

@TestConfiguration(proxyBeanMethods = false)
class IntegrationTestConfig {

    @Bean
    @ServiceConnection
    fun postgres(): PostgreSQLContainer = PostgreSQLContainer("postgres:17")

    @Bean
    @Primary
    fun testClock() = MutableClock(START)

    @Bean
    fun fakeKakaoVerifier(): SocialTokenVerifierPort = FakeSocialTokenVerifier(SocialProvider.KAKAO)

    @Bean
    fun fakeGoogleVerifier(): SocialTokenVerifierPort = FakeSocialTokenVerifier(SocialProvider.GOOGLE)

    companion object {
        val START: Instant = Instant.parse("2026-10-01T00:00:00Z")
    }
}
