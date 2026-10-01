package kindl.domain.verification.repository

import kindl.domain.verification.entity.Cheer
import org.springframework.data.jpa.repository.JpaRepository

interface CheerRepository : JpaRepository<Cheer, Long>
