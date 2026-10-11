package proov.domain.user.port

import proov.domain.user.User

interface RoomUserPort {
    fun findAllByIds(userIds: Collection<String>): List<User>
}
