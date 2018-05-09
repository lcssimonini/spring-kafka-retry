package io.zup.springframework.kafka.listener

import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.listener.KafkaListenerErrorHandler
import org.springframework.kafka.listener.ListenerExecutionFailedException
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.Message
import org.springframework.messaging.support.MessageBuilder

class KafkaRetryPolicyErrorHandler<K, V>(
    private val template: KafkaTemplate<K, V>
): KafkaListenerErrorHandler {

    private var retryTopic: String? = null
    private var maxRetries: Int? = null

    companion object {
        const val REMAINING_RETRIES_HEADER = "remaining-retries"
    }

    fun send(message: Message<*>) {
        template.send(message)
    }

    fun withRetryTopic(retryTopic: String): KafkaRetryPolicyErrorHandler<K, V> =
        this
            .apply { this.retryTopic = retryTopic }

    fun withMaxRetries(maxRetries: Int): KafkaRetryPolicyErrorHandler<K, V> =
        this
            .apply { this.maxRetries = maxRetries }

    override fun handleError(message: Message<*>, exception: ListenerExecutionFailedException): Any {
        println("Retrying message due to error: ${exception.message}")

        val remainingRetries = remainingRetries(message)
        val retryTopic = retryTopic(message)

        if (remainingRetries > 0) {

            MessageBuilder
                .fromMessage(message)
                .removeHeader(KafkaHeaders.TOPIC)
                .removeHeader(KafkaHeaders.PARTITION_ID)
                .removeHeader(KafkaHeaders.MESSAGE_KEY)
                .setHeader(KafkaHeaders.TOPIC, retryTopic)
                .setHeader(REMAINING_RETRIES_HEADER, remainingRetries - 1)
                //.setHeader("retry_timestamp", Instant.now())
                .build()
                .let { send(it) }

        } else {
            // dlq
            MessageBuilder
                .fromMessage(message)
                .removeHeader(KafkaHeaders.TOPIC)
                .removeHeader(KafkaHeaders.PARTITION_ID)
                .removeHeader(KafkaHeaders.MESSAGE_KEY)
                .setHeader(KafkaHeaders.TOPIC, "dlq-topic")
                .setHeader(REMAINING_RETRIES_HEADER, 0)
                //.setHeader("retry_timestamp", Instant.now())
                .build()
                .let { send(it) }
        }

        return "ok"
    }

    private fun remainingRetries(message: Message<*>): Int =
        message.headers?.get(REMAINING_RETRIES_HEADER) as? Int ?: maxRetries ?: throw IllegalArgumentException("Maximum retries argument not provided")

    private fun retryTopic(message: Message<*>): String =
        retryTopic ?: message.headers?.get(KafkaHeaders.RECEIVED_TOPIC) as? String ?: throw IllegalArgumentException("Retry topic argument not provided")
}
