package kindl.domain.room.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import kindl.core.error.KindlException
import kindl.domain.room.enums.RoomStatus
import kindl.domain.room.error.RoomError
import kindl.domain.room.vo.InviteCode
import kindl.domain.room.vo.RoomName
import kindl.support.entity.BaseSoftDeleteEntity
import kindl.support.id.TsidId
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.SqlTypes

@Entity
@Table(name = "rooms")
@SQLRestriction("deleted_at IS NULL")
class Room private constructor(
    name: RoomName,
    inviteCode: InviteCode,
    ownerUserId: String,
    clientRequestId: String,
) : BaseSoftDeleteEntity() {

    @Id @TsidId
    @Column(length = 13, updatable = false)
    var id: String? = null
        protected set

    // 규칙은 그래핌 15개, 컬럼 길이는 상한 안전망
    @Column(nullable = false, length = 60)
    var name: String = name.value
        protected set

    @Column(length = 13)
    var imageFileId: String? = null
        protected set

    @Column(nullable = false, updatable = false, columnDefinition = "char(8)")
    val inviteCode: String = inviteCode.value

    @Column(nullable = false, length = 13)
    var ownerUserId: String = ownerUserId
        protected set

    // 정원 검사용. 입장·퇴장 때 rooms 행을 잠근 뒤 갱신한다
    @JdbcTypeCode(SqlTypes.TINYINT)
    @Column(nullable = false)
    var memberCount: Int = 1
        protected set

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: RoomStatus = RoomStatus.ACTIVE
        protected set

    var endedAt: Instant? = null
        protected set

    // 종료 시점 모임 평균 달성률 스냅샷
    @JdbcTypeCode(SqlTypes.TINYINT)
    var finalAvgRate: Int? = null
        protected set

    // 앱이 만든 Idempotency-Key. 버튼 연타·재시도에도 모임이 하나만 생기게
    @Column(length = 36, updatable = false)
    val clientRequestId: String? = clientRequestId

    val isEnded: Boolean get() = status == RoomStatus.ENDED

    fun isOwnedBy(userId: String): Boolean = ownerUserId == userId

    fun requireOwner(userId: String) {
        if (!isOwnedBy(userId)) throw KindlException(RoomError.OWNER_ONLY)
    }

    fun requireActive() {
        if (isEnded) throw KindlException(RoomError.ROOM_ENDED)
    }

    fun addMember() {
        requireActive()
        if (isFull) throw KindlException(RoomError.ROOM_FULL)
        memberCount++
    }

    val isFull: Boolean get() = memberCount >= CAPACITY

    fun removeMember() {
        check(memberCount > 0) { "멤버 수가 0보다 작아질 수 없습니다." }
        memberCount--
    }

    fun handOverTo(userId: String) {
        ownerUserId = userId
    }

    /** 평균 스냅샷은 공약 달성률 계산이 붙으면 채운다 */
    fun end(now: Instant, avgRate: Int? = null) {
        requireActive()
        status = RoomStatus.ENDED
        endedAt = now
        finalAvgRate = avgRate
    }

    companion object {
        const val CAPACITY = 10

        // 만든 사람이 첫 멤버(모임장)다
        fun create(name: RoomName, ownerUserId: String, inviteCode: InviteCode, clientRequestId: String) =
            Room(name, inviteCode, ownerUserId, clientRequestId)
    }
}
