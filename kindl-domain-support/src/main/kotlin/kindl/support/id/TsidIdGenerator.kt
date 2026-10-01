package kindl.support.id

import kindl.core.id.Tsids
import org.hibernate.engine.spi.SharedSessionContractImplementor
import org.hibernate.generator.BeforeExecutionGenerator
import org.hibernate.generator.EventType
import org.hibernate.generator.EventTypeSets
import org.hibernate.generator.GeneratorCreationContext
import java.lang.reflect.Field
import java.lang.reflect.Member
import java.lang.reflect.Method
import java.util.EnumSet

class TsidIdGenerator(
    @Suppress("UNUSED_PARAMETER") config: TsidId,
    member: Member,
    @Suppress("UNUSED_PARAMETER") context: GeneratorCreationContext,
) : BeforeExecutionGenerator {

    private val asString: Boolean = when (member) {
        is Field -> member.type == String::class.java
        is Method -> member.returnType == String::class.java
        else -> error("@TsidId는 필드나 게터에만 붙일 수 있습니다: $member")
    }

    override fun generate(
        session: SharedSessionContractImplementor,
        owner: Any,
        currentValue: Any?,
        eventType: EventType,
    ): Any = if (asString) Tsids.nextString() else Tsids.nextLong()

    override fun getEventTypes(): EnumSet<EventType> = EventTypeSets.INSERT_ONLY
}
