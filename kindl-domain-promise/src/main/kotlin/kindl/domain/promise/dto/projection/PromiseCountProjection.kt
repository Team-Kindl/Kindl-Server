package kindl.domain.promise.dto.projection

interface PromiseCountProjection {
    val roomId: String
    val userId: String
    val count: Long
}
