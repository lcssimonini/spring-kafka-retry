import io.zup.springframework.kafka.ui.configuration.KafkaDLQConfiguration
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Import

@Import(KafkaDLQConfiguration::class)
@SpringBootApplication
@ComponentScan(
    basePackages = [
        "io.zup.springframework.kafka.ui"
    ]
)
open class KafkaDLQApplication

fun main(args: Array<String>) {
    SpringApplication.run(KafkaDLQApplication::class.java, *args)
}
