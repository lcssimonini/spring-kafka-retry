package io.cvortex.spring.kafka.listener

import io.cvortex.spring.kafka.annotation.BackOffStrategy
import io.cvortex.spring.kafka.annotation.RetryPolicy
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.listener.KafkaListenerErrorHandler
import org.springframework.kafka.listener.ListenerExecutionFailedException
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.Message
import org.springframework.messaging.support.MessageBuilder
import java.time.Clock
import org.springframework.core.env.Environment

class KafkaRetryPolicyErrorHandler<K, V>(
        private val template: KafkaTemplate<K, V>,
        private val maxRetries: Int,
        private val retryTopic: String,
        private val retryInterval: Long,
        private val dlqTopic: String,
        private val backOffStrategy: BackOffStrategy,
        private val clock: Clock = Clock.systemUTC()
): KafkaListenerErrorHandler {

    companion object {
        val logger: Logger = LoggerFactory.getLogger(KafkaRetryPolicyErrorHandler::class.java)
        const val REMAINING_RETRIES_HEADER = "remaining-retries"
        const val RETRY_TIMESTAMP_HEADER = "retry-timestamp"

        internal fun resolveTopicName(environment: Environment, key: String) =
            environment.resolvePlaceholders(key)

        fun <K, V> from(policy: RetryPolicy, template: KafkaTemplate<K, V>, environment: Environment): KafkaRetryPolicyErrorHandler<K, V> =
                KafkaRetryPolicyErrorHandler(
                        template = template,
                        maxRetries = policy.retries,
                        retryTopic = resolveTopicName(environment, policy.topic),
                        retryInterval = policy.retryInterval,
                        dlqTopic = resolveTopicName(environment, policy.dlqTopic),
                        backOffStrategy = policy.backoffStrategy
                )
    }

    override fun handleError(message: Message<*>, exception: ListenerExecutionFailedException): Any {

        val remainingRetries = remainingRetries(message)

        val messageBuilder = MessageBuilder
            .fromMessage(message)
            .removeHeader(KafkaHeaders.TOPIC)
            .removeHeader(KafkaHeaders.PARTITION_ID)
            .removeHeader(KafkaHeaders.MESSAGE_KEY)
            .setHeader(KafkaHeaders.TOPIC, targetTopic(remainingRetries))


        if (remainingRetries > 0) {

            logger.info("Message handling retrial due to error. [Retrial: ${maxRetries - remainingRetries + 1} of $maxRetries - Topic: $retryTopic]", exception)

            messageBuilder
                .setHeader(REMAINING_RETRIES_HEADER, remainingRetries - 1)
                .setHeader(RETRY_TIMESTAMP_HEADER, retryTimestamp(remainingRetries))

        } else {

            logger.info("Maximum message handling retries reached. Forwarding message to DLQ topic [Topic: $retryTopic - DLQ Topic: $dlqTopic]", exception)
        }

        template.send(messageBuilder.build())

        return remainingRetries
    }

    private fun targetTopic(remainingRetries: Int): String =
        when {
            remainingRetries > 0 -> retryTopic
            else -> dlqTopic
        }

    private fun remainingRetries(message: Message<*>): Int =
        message.headers?.get(REMAINING_RETRIES_HEADER) as? Int ?: maxRetries

    private fun retryTimestamp(remainingRetries: Int): Long =
        backOffStrategy.calculateBackoffTimeInSeconds(clock, maxRetries - remainingRetries, retryInterval)
}
