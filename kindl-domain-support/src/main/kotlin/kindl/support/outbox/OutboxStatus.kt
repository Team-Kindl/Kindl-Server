package kindl.support.outbox

enum class OutboxStatus {
    PENDING,
    PROCESSING,
    DONE,
    FAILED,
}
