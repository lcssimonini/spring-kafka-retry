package io.zup.springframework.kafka.ui

import io.zup.springframework.kafka.ui.configuration.KafkaDLQConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Import

@Import(KafkaDLQConfiguration::class)
@SpringBootApplication
open class KafkaDLQUITestApplication