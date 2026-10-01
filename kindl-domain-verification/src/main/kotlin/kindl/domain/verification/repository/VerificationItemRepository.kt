package kindl.domain.verification.repository

import kindl.domain.verification.entity.VerificationItem
import org.springframework.data.jpa.repository.JpaRepository

interface VerificationItemRepository : JpaRepository<VerificationItem, Long>
