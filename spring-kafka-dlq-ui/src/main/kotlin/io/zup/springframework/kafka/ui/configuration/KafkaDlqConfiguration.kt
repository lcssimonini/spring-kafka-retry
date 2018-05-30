package io.zup.springframework.kafka.ui.configuration

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.ComponentScan
import org.springframework.web.servlet.config.annotation.EnableWebMvc

@SpringBootApplication
@EnableWebMvc
@ComponentScan("io.zup.springframework.kafka.ui")
open class KafkaDlqConfiguration

fun main(args: Array<String>) {
    SpringApplication.run(KafkaDlqConfiguration::class.java, *args)
}