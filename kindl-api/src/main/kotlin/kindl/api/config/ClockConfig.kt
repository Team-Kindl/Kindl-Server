package kindl.api.config

import kindl.core.time.ServiceClock
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Clock

/** 시각은 전부 이 Clock에서 얻는다. 테스트는 Clock.fixed()로 바꿔 자정 경계를 재현한다. */
@Configuration
class ClockConfig {

    @Bean
    fun clock(): Clock = Clock.systemUTC()

    @Bean
    fun serviceClock(clock: Clock) = ServiceClock(clock)
}
