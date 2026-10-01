package kindl.domain.verification.repository

import kindl.domain.verification.entity.Report
import org.springframework.data.jpa.repository.JpaRepository

interface ReportRepository : JpaRepository<Report, Long>
