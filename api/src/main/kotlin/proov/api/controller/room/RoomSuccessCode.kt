package proov.api.controller.room

import proov.api.common.response.SuccessCode

enum class RoomSuccessCode(
    override val status: Int,
    override val message: String,
) : SuccessCode {
    CREATE_ROOM(201, "모임을 만들었습니다."),
    GET_MY_ROOMS(200, "내 모임 목록을 조회했습니다."),
    GET_ROOM(200, "모임 정보를 조회했습니다."),
    GET_MEMBERS(200, "모임 멤버를 조회했습니다."),
    LEAVE_ROOM(200, "모임에서 나갔습니다."),
    KICK_MEMBER(200, "멤버를 내보냈습니다."),
    END_ROOM(200, "모임을 종료했습니다."),
    ;

    override val code: String get() = name
}
