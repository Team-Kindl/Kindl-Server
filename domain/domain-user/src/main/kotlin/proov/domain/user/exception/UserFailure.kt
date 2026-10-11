package proov.domain.user.exception

import proov.core.code.FailureCode

enum class UserFailure(
    override val status: Int,
    override val message: String,
) : FailureCode {
    NICKNAME_LENGTH(400, "닉네임은 15자까지 쓸 수 있어요"),
    NICKNAME_INVALID_CHAR(400, "닉네임에는 이모지를 쓸 수 없어요"),
    NICKNAME_BLOCKED(400, "사용할 수 없는 닉네임이에요"),
    NICKNAME_TAKEN(409, "이미 사용 중인 닉네임이에요"),
    INVALID_TIMEZONE(400, "지원하지 않는 시간대예요"),
    INVALID_LOCALE(400, "지원하지 않는 언어예요"),
    USER_NOT_FOUND(401, "존재하지 않는 계정이에요"),
    ;

    override val code: String get() = name
}
