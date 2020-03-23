package io.cvortex.spring.kafka.helper

import io.cvortex.spring.kafka.annotation.BackOffStrategy
import java.time.Instant

object TestConstants {

    const val MAIN_TOPIC = "main-topic"

    const val RETRY_TOPIC = "retry-topic"

    const val RETRY_INTERVAL = 10L

    const val DLQ_TOPIC = "dlq-topic"

    const val MAX_RETRIES = 3

    val BASE_INSTANT = Instant.parse("1986-07-05T09:00:00Z")

    val BACKOFF_STRATEGY = BackOffStrategy.CONSTANT

}