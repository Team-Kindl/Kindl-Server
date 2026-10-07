package kindl.support

import kindl.core.type.SocialProvider
import kindl.domain.auth.fixture.FakeSocialTokenVerifier
import kindl.domain.auth.port.SocialTokenVerifier
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.testcontainers.mysql.MySQLContainer
import java.time.Instant

/**
 * 통합 테스트 공통: 실제 MySQL 8.4 컨테이너 + Flyway V1, 고정 시계, 외부 호출 없는 소셜 검증기.
 * H2는 쓰지 않는다 — 생성 컬럼 유니크, FOR UPDATE가 다르게 동작한다.
 */
@TestConfiguration(proxyBeanMethods = false)
class IntegrationTestConfig {

    @Bean
    @ServiceConnection
    fun mysql(): MySQLContainer = MySQLContainer("mysql:8.4")
        .withCommand("--character-set-server=utf8mb4", "--collation-server=utf8mb4_0900_ai_ci", "--default-time-zone=+00:00")

    @Bean
    @Primary
    fun testClock() = MutableClock(START)

    @Bean
    fun fakeKakaoVerifier(): SocialTokenVerifier = FakeSocialTokenVerifier(SocialProvider.KAKAO)

    @Bean
    fun fakeGoogleVerifier(): SocialTokenVerifier = FakeSocialTokenVerifier(SocialProvider.GOOGLE)

    companion object {
        val START: Instant = Instant.parse("2026-10-01T00:00:00Z")
    }
}
