package proov.domain.room.port

import proov.domain.room.Room

interface RoomRepositoryPort {
    fun create(room: Room): Room

    fun update(room: Room): Room

    fun delete(roomId: String)

    fun findById(roomId: String): Room?

    fun findByIdForUpdate(roomId: String): Room?

    fun findByInviteCode(inviteCode: String): Room?

    fun findByInviteCodeForUpdate(inviteCode: String): Room?

    fun findByClientRequestId(clientRequestId: String): Room?

    fun existsByInviteCode(inviteCode: String): Boolean
}
