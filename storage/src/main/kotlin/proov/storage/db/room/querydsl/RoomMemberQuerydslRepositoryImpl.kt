package proov.storage.db.room.querydsl

import com.querydsl.core.types.Projections
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.stereotype.Repository
import proov.storage.db.room.entity.QRoomEntity.Companion.roomEntity
import proov.storage.db.room.entity.QRoomMemberEntity.Companion.roomMemberEntity
import proov.storage.db.room.projection.MyRoomProjection

@Repository
class RoomMemberQuerydslRepositoryImpl(
    private val queryFactory: JPAQueryFactory,
) : RoomMemberQuerydslRepository {

    override fun findMyRooms(userId: String): List<MyRoomProjection> =
        queryFactory
            .select(
                Projections.constructor(
                    MyRoomProjection::class.java,
                    roomEntity.id,
                    roomEntity.name,
                    roomEntity.inviteCode,
                    roomEntity.ownerUserId,
                    roomEntity.memberCount,
                    roomEntity.status,
                    roomEntity.endedAt,
                    roomEntity.clientRequestId,
                    roomMemberEntity.role,
                    roomMemberEntity.joinedAt,
                ),
            )
            .from(roomMemberEntity)
            .join(roomEntity).on(roomEntity.id.eq(roomMemberEntity.roomId))
            .where(roomMemberEntity.userId.eq(userId))
            .fetch()
}
