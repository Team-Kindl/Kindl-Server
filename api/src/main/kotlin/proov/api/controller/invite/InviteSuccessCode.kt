package proov.api.controller.invite

import proov.api.common.response.SuccessCode

enum class InviteSuccessCode(
    override val status: Int,
    override val message: String,
) : SuccessCode {
    PREVIEW_INVITE(200, "초대 정보를 조회했습니다."),
    JOIN_ROOM(200, "모임에 들어갔습니다."),
    ;

    override val code: String get() = name
}
