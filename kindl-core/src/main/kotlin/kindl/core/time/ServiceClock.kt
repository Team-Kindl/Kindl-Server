package kindl.core.time

import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * 서버 시각은 전부 UTC Instant. 시간대가 들어가는 곳은 "유저의 오늘"을 계산하는 여기뿐이다.
 */
class ServiceClock(
    private val clock: Clock,
) {
    fun now(): Instant = clock.instant()

    fun today(zone: ZoneId): LocalDate = LocalDate.ofInstant(clock.instant(), zone)
}
