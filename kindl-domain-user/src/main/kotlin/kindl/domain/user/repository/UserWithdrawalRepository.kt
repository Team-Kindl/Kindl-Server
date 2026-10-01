package kindl.domain.user.repository

import kindl.domain.user.entity.UserWithdrawal
import org.springframework.data.jpa.repository.JpaRepository

interface UserWithdrawalRepository : JpaRepository<UserWithdrawal, Long>
