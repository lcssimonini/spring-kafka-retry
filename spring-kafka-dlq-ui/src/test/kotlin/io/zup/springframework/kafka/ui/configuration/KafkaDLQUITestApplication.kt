package io.zup.springframework.kafka.ui.configuration

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Import

@Import(KafkaDLQConfiguration::class)
@SpringBootApplication
open class KafkaDLQUITestApplication