package io.zup.springframework.kafka.ui

import io.zup.springframework.kafka.ui.configuration.KafkaDLQConfiguration
import io.zup.springframework.kafka.ui.integrated.consumer.RepublishConsumer
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import

@Import(KafkaDLQConfiguration::class)
@SpringBootApplication
open class KafkaDLQUITestApplication {

    @Bean
    open fun republishMessageConsumer(): RepublishConsumer {
        return RepublishConsumer()
    }
}