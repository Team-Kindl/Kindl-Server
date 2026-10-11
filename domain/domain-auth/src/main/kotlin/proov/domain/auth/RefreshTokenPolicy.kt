package proov.domain.auth

import java.time.Duration

data class RefreshTokenPolicy(
    val ttl: Duration,
    val reuseGrace: Duration,
)
