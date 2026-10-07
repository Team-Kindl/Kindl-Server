package kindl.domain.room.vo

import kindl.core.error.KindlException
import kindl.core.text.DisplayText
import kindl.domain.room.error.RoomError

/** 모임 이름: 앞뒤 공백 제거 후 그래핌 1~15자, 이모지 불가 (닉네임과 같은 기준, 중복은 허용) */
@JvmInline
value class RoomName private constructor(val value: String) {
    companion object {
        const val MAX_GRAPHEMES = 15

        fun of(raw: String): RoomName {
            val normalized = DisplayText.normalize(raw)
            if (DisplayText.graphemeCount(normalized, MAX_GRAPHEMES) !in 1..MAX_GRAPHEMES) {
                throw KindlException(RoomError.ROOM_NAME_LENGTH)
            }
            if (DisplayText.containsForbiddenChar(normalized)) throw KindlException(RoomError.ROOM_NAME_INVALID_CHAR)
            return RoomName(normalized)
        }
    }
}
