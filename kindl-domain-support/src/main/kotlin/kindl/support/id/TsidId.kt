package kindl.support.id

import org.hibernate.annotations.IdGeneratorType

/**
 * 앱이 만든 TSID를 persist 시점에 SQL 없이 채운다.
 * 필드 타입이 String이면 Crockford base32 13자(API 노출용), Long이면 BIGINT(내부 전용).
 */
@IdGeneratorType(TsidIdGenerator::class)
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
annotation class TsidId
