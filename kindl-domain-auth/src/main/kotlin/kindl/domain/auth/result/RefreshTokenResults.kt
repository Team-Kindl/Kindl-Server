package kindl.domain.auth.result

import java.time.Instant

/** 원문은 발급 응답에서 한 번만 나간다. */
data class IssuedRefreshToken(
    val token: String,
    val expiresAt: Instant,
)

sealed interface RefreshRotation {
    data class Rotated(val userId: String, val deviceId: Long, val issued: IssuedRefreshToken) : RefreshRotation

    // 없거나, 만료됐거나, 이미 폐기됨
    data object Invalid : RefreshRotation

    // 회전 직후 유예 시간 안의 재요청 (네트워크 재시도 등)
    data object Race : RefreshRotation

    // 유예가 지난 뒤 옛 토큰 재사용 → 묶음 전체를 폐기했다
    data object Reused : RefreshRotation
}
