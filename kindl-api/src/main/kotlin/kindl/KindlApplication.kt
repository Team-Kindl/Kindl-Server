package kindl

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import java.util.TimeZone

@SpringBootApplication
class KindlApplication

fun main(args: Array<String>) {
    // 누가 LocalDateTime.now()를 실수로 써도 서버마다 값이 다르지 않게
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
    runApplication<KindlApplication>(*args)
}
