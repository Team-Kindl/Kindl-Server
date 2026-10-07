package kindl.domain.auth.service

import java.time.Duration

data class RefreshTokenPolicy(
    val ttl: Duration,
    val reuseGrace: Duration,
)
