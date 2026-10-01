package kindl.domain.promise.repository

import kindl.domain.promise.entity.PromiseDailyRecord
import org.springframework.data.jpa.repository.JpaRepository

interface PromiseDailyRecordRepository : JpaRepository<PromiseDailyRecord, Long>
