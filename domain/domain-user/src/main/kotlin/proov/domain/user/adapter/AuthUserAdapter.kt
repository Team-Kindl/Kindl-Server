package proov.domain.user.adapter

import org.springframework.stereotype.Component
import proov.domain.user.Device
import proov.domain.user.User
import proov.domain.user.command.CreateUserCommand
import proov.domain.user.command.DeviceCommand
import proov.domain.user.port.AuthUserPort
import proov.domain.user.service.DeviceService
import proov.domain.user.service.UserCommandService
import proov.domain.user.service.UserQueryService
import java.time.Instant

@Component
class AuthUserAdapter(
    private val userCommandService: UserCommandService,
    private val userQueryService: UserQueryService,
    private val deviceService: DeviceService,
) : AuthUserPort {

    override fun createUser(command: CreateUserCommand, now: Instant): User = userCommandService.create(command, now)

    override fun requireExists(userId: String) = userQueryService.requireExists(userId)

    override fun syncDevice(userId: String, command: DeviceCommand, now: Instant): Device =
        deviceService.sync(userId, command, now)
}
