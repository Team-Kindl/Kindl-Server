package kindl.api.common.ratelimit

import kindl.core.error.CommonError
import kindl.core.error.KindlException
import org.springframework.stereotype.Component
import java.time.Clock
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

/**
 * 1분 고정 창 호출 제한. 카운터는 인스턴스 메모리에 둔다.
 *
 * Redis 분산 카운터를 쓰지 않는 이유: 막으려는 것은 초대 코드 무작위 대입과 닉네임 긁기다.
 * 서버 2대면 실제 한도가 2배가 되지만, 초대 코드 공간(32^8 ≈ 1.1조)에서는 분당 40회여도 대입이 사실상 불가능하다.
 * Redis를 이 용도 하나로 두면 비용과 장애 지점만 늘고, 서버 재시작으로 카운터가 비는 것도 허용 범위다.
 */
@Component
class RateLimiter(
    private val clock: Clock,
) {
    private val windows = ConcurrentHashMap<String, Window>()

    fun check(key: String, limitPerMinute: Int) {
        val minute = clock.millis() / MINUTE_MILLIS
        val window = windows.compute(key) { _, current ->
            if (current == null || current.minute != minute) Window(minute) else current
        }!!
        if (window.count.incrementAndGet() > limitPerMinute) throw KindlException(CommonError.RATE_LIMITED, "key=$key")
        if (windows.size > MAX_KEYS) windows.entries.removeIf { it.value.minute < minute }
    }

    private class Window(val minute: Long) {
        val count = AtomicInteger()
    }

    companion object {
        private const val MINUTE_MILLIS = 60_000L

        // 지난 창의 키를 정리하는 기준. 동시 사용자 수 × 제한 대상 API 수보다 넉넉하게
        private const val MAX_KEYS = 10_000
    }
}
