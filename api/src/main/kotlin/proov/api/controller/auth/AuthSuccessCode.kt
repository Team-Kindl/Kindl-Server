package proov.api.controller.auth

import proov.api.common.response.SuccessCode

enum class AuthSuccessCode(
    override val status: Int,
    override val message: String,
) : SuccessCode {
    LOGIN(200, "로그인 결과를 조회했습니다."),
    REFRESH(200, "토큰을 재발급했습니다."),
    LOGOUT(200, "로그아웃했습니다."),
    ;

    override val code: String get() = name
}
