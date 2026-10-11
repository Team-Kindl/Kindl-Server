package proov.api.common.ratelimit

import org.springframework.stereotype.Component
import proov.api.common.exception.CommonFailure
import proov.core.exception.ProovException
import java.time.Clock
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

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
        if (window.count.incrementAndGet() > limitPerMinute) throw ProovException(CommonFailure.RATE_LIMITED, "key=$key")
        if (windows.size > MAX_KEYS) windows.entries.removeIf { it.value.minute < minute }
    }

    private class Window(val minute: Long) {
        val count = AtomicInteger()
    }

    companion object {
        private const val MINUTE_MILLIS = 60_000L
        private const val MAX_KEYS = 10_000
    }
}
