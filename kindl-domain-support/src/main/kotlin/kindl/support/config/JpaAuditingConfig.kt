package kindl.support.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.auditing.DateTimeProvider
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import java.time.Clock
import java.util.Optional

/**
 * Auditing이 시스템 시계가 아니라 Clock 빈을 쓰게 한다 → 테스트에서 시각을 고정할 수 있다.
 * 벌크 UPDATE는 Auditing을 거치지 않으므로 updated_at = :now를 직접 넣는다.
 */
@Configuration
@EnableJpaAuditing(dateTimeProviderRef = "auditingDateTimeProvider")
class JpaAuditingConfig {

    @Bean
    fun auditingDateTimeProvider(clock: Clock) = DateTimeProvider { Optional.of(clock.instant()) }
}
