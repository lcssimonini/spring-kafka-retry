package io.cvortex.spring.kafka.annotation

annotation class RetryKafkaListener(
    val retryPolicyId: String
)
