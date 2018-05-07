package io.zup.springframework.kafka.annotation

annotation class RetryPolicy(
    val topic: String,
    val retries: Int,
    val dlqTopic: String
)