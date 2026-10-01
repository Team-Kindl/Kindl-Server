package kindl.support.converter

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter
import java.time.ZoneId

/** IANA 시간대 이름(예: Asia/Seoul)으로 저장한다. */
@Converter
class ZoneIdConverter : AttributeConverter<ZoneId, String> {
    override fun convertToDatabaseColumn(attribute: ZoneId?): String? = attribute?.id

    override fun convertToEntityAttribute(dbData: String?): ZoneId? = dbData?.let(ZoneId::of)
}
