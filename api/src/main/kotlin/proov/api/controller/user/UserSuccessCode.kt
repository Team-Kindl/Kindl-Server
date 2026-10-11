package proov.api.controller.user

import proov.api.common.response.SuccessCode

enum class UserSuccessCode(
    override val status: Int,
    override val message: String,
) : SuccessCode {
    SIGNUP(201, "회원가입이 완료되었습니다."),
    GET_ME(200, "내 정보를 조회했습니다."),
    CHECK_NICKNAME(200, "닉네임 사용 가능 여부를 조회했습니다."),
    ;

    override val code: String get() = name
}
