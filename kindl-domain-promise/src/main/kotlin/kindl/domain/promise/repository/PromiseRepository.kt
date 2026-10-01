package kindl.domain.promise.repository

import kindl.domain.promise.entity.Promise
import org.springframework.data.jpa.repository.JpaRepository

interface PromiseRepository : JpaRepository<Promise, String>
