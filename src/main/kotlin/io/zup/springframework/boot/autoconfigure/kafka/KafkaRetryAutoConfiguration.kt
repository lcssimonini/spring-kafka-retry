package io.zup.springframework.boot.autoconfigure.kafka

import io.zup.springframework.kafka.config.KafkaRetryConfiguration
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@Configuration
@Import(KafkaRetryConfiguration::class)
open class KafkaRetryAutoConfiguration