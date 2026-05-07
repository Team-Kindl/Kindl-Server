package kindl

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class KindlApplication

fun main(args: Array<String>) {
	runApplication<KindlApplication>(*args)
}
