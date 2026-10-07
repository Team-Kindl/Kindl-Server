package kindl.domain.room.repository

import com.querydsl.core.types.Projections
import com.querydsl.jpa.impl.JPAQueryFactory
import kindl.domain.room.dto.projection.MyRoomProjection
import kindl.domain.room.entity.QRoom.Companion.room
import kindl.domain.room.entity.QRoomMember.Companion.roomMember

class RoomMemberRepositoryCustomImpl(
    private val queryFactory: JPAQueryFactory,
) : RoomMemberRepositoryCustom {

    // 지워진 모임·멤버십은 두 엔티티의 @SQLRestriction이 걸러 낸다
    override fun findMyRooms(userId: String): List<MyRoomProjection> =
        queryFactory
            .select(
                Projections.constructor(
                    MyRoomProjection::class.java,
                    room.id,
                    room.name,
                    room.imageFileId,
                    room.inviteCode,
                    room.ownerUserId,
                    room.status,
                    room.memberCount,
                    room.endedAt,
                    roomMember.role,
                    roomMember.joinedAt,
                    roomMember.lastVerifiedAt,
                ),
            )
            .from(roomMember)
            .join(room).on(room.id.eq(roomMember.roomId))
            .where(roomMember.userId.eq(userId))
            .fetch()
}
