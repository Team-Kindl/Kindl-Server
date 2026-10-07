package kindl.domain.user.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import kindl.core.type.Platform
import kindl.support.entity.BaseSoftDeleteEntity
import kindl.support.id.TsidId
import org.hibernate.annotations.SQLRestriction
import java.time.Instant

/**
 * 앱 설치 하나. installationId는 설치마다 앱이 만든 UUID다.
 * 기기별 로그아웃(refresh token 묶음)과 푸시 토큰 등록의 기준이다.
 * 권한 상태(알림·카메라)는 서버가 쓰는 곳이 없어 저장하지 않는다. 알림 권한은 푸시 토큰 등록 때 함께 받는다.
 */
@Entity
@Table(name = "devices")
@SQLRestriction("deleted_at IS NULL")
class Device private constructor(
    userId: String,
    installationId: String,
    spec: DeviceSpec,
    now: Instant,
) : BaseSoftDeleteEntity() {

    @Id @TsidId
    var id: Long? = null
        protected set

    @Column(nullable = false, length = 13, updatable = false)
    val userId: String = userId

    @Column(nullable = false, length = 64, updatable = false)
    val installationId: String = installationId

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var platform: Platform = spec.platform
        protected set

    @Column(nullable = false, length = 20)
    var osVersion: String = spec.osVersion
        protected set

    @Column(nullable = false, length = 20)
    var appVersion: String = spec.appVersion
        protected set

    // 3단계
    @Column(length = 255)
    var pushToken: String? = null
        protected set

    @Column(nullable = false)
    var lastSeenAt: Instant = now
        protected set

    fun sync(spec: DeviceSpec, now: Instant) {
        platform = spec.platform
        osVersion = spec.osVersion
        appVersion = spec.appVersion
        lastSeenAt = now
    }

    companion object {
        fun register(userId: String, installationId: String, spec: DeviceSpec, now: Instant) =
            Device(userId, installationId, spec, now)
    }
}

data class DeviceSpec(
    val platform: Platform,
    val osVersion: String,
    val appVersion: String,
)
