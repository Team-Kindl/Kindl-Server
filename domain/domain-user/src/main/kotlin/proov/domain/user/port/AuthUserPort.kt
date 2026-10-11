package proov.domain.user.port

import proov.domain.user.Device
import proov.domain.user.User
import proov.domain.user.command.CreateUserCommand
import proov.domain.user.command.DeviceCommand
import java.time.Instant

interface AuthUserPort {
    fun createUser(command: CreateUserCommand, now: Instant): User

    fun requireExists(userId: String)

    fun syncDevice(userId: String, command: DeviceCommand, now: Instant): Device
}
