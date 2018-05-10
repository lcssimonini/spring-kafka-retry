package io.zup.springframework.kafka.listener

import io.zup.springframework.kafka.annotation.BackoffStrategy
import io.zup.springframework.kafka.annotation.RetryPolicy
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.listener.KafkaListenerErrorHandler
import org.springframework.kafka.listener.ListenerExecutionFailedException
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.Message
import org.springframework.messaging.support.MessageBuilder

class KafkaRetryPolicyErrorHandler<K, V>(
    private val template: KafkaTemplate<K, V>,
    private val maxRetries: Int,
    private val retryTopic: String,
    private val dlqTopic: String,
    private val backoffStrategy: BackoffStrategy
): KafkaListenerErrorHandler {

    companion object {
        val logger: Logger = LoggerFactory.getLogger(KafkaRetryPolicyErrorHandler::class.java)
        const val REMAINING_RETRIES_HEADER = "remaining-retries"

        fun <K, V> from(policy: RetryPolicy, template: KafkaTemplate<K, V>): KafkaRetryPolicyErrorHandler<K, V> =
            KafkaRetryPolicyErrorHandler(
                template = template,
                maxRetries = policy.retries,
                retryTopic = policy.topic,
                dlqTopic = policy.dlqTopic,
                // FIXME: use strategy accordingly to annotation parameter
                backoffStrategy = BackoffStrategy.CONSTANT
            )
    }

    fun send(message: Message<*>) {
        template.send(message)
    }

    override fun handleError(message: Message<*>, exception: ListenerExecutionFailedException): Any {

        val remainingRetries = remainingRetries(message)

        if (remainingRetries > 0) {

            logger.info("Message handling retrial due to error. [Retrial: ${maxRetries - remainingRetries + 1} of $maxRetries - Topic: $retryTopic]", exception)

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
                .setHeader(KafkaHeaders.TOPIC, dlqTopic)
                .setHeader(REMAINING_RETRIES_HEADER, 0)
                //.setHeader("retry_timestamp", Instant.now())
                .build()
                .let { send(it) }
        }

        return remainingRetries
    }

    private fun remainingRetries(message: Message<*>): Int =
        message.headers?.get(REMAINING_RETRIES_HEADER) as? Int ?: maxRetries
}
