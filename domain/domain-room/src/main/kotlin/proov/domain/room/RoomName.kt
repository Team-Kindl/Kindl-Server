package proov.domain.room

import proov.core.exception.ProovException
import proov.core.text.DisplayText
import proov.domain.room.exception.RoomFailure

@JvmInline
value class RoomName private constructor(val value: String) {
    companion object {
        const val MAX_GRAPHEMES = 15

        fun of(raw: String): RoomName {
            val normalized = DisplayText.normalize(raw)
            if (DisplayText.graphemeCount(normalized, MAX_GRAPHEMES) !in 1..MAX_GRAPHEMES) {
                throw ProovException(RoomFailure.ROOM_NAME_LENGTH)
            }
            if (DisplayText.containsForbiddenChar(normalized)) throw ProovException(RoomFailure.ROOM_NAME_INVALID_CHAR)
            return RoomName(normalized)
        }

        fun restore(value: String) = RoomName(value)
    }
}
