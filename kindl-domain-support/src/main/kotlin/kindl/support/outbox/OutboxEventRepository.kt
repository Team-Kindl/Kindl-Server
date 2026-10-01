package kindl.support.outbox

import org.springframework.data.jpa.repository.JpaRepository

interface OutboxEventRepository : JpaRepository<OutboxEvent, Long>
