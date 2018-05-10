package io.zup.springframework.kafka.annotation

annotation class RetryKafkaListener(
    val retryPolicyId: String
)
