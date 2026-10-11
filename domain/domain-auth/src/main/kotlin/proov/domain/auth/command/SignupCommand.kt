package proov.domain.auth.command

import proov.domain.user.command.CreateUserCommand
import proov.domain.user.command.DeviceCommand

data class SignupCommand(
    val signupToken: String,
    val user: CreateUserCommand,
    val device: DeviceCommand,
)
