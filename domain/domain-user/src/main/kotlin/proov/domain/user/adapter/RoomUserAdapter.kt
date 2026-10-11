package proov.domain.user.adapter

import org.springframework.stereotype.Component
import proov.domain.user.User
import proov.domain.user.port.RoomUserPort
import proov.domain.user.service.UserQueryService

@Component
class RoomUserAdapter(
    private val userQueryService: UserQueryService,
) : RoomUserPort {

    override fun findAllByIds(userIds: Collection<String>): List<User> = userQueryService.getAllByIds(userIds)
}
