package kindl.domain.verification.repository

import kindl.domain.verification.entity.Verification
import org.springframework.data.jpa.repository.JpaRepository

interface VerificationRepository : JpaRepository<Verification, String>
