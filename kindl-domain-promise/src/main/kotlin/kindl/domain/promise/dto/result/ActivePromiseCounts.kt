package kindl.domain.promise.dto.result

data class ActivePromiseCounts(
    private val byRoomAndUser: Map<String, Map<String, Int>>,
) {
    fun ofRoom(roomId: String): Int = byRoomAndUser[roomId]?.values?.sum() ?: 0

    fun of(roomId: String, userId: String): Int = byRoomAndUser[roomId]?.get(userId) ?: 0
}
