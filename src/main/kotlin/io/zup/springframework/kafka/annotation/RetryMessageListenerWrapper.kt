package io.zup.springframework.kafka.annotation

import io.zup.springframework.kafka.listener.KafkaRetryPolicyErrorHandler
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.runBlocking
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.kafka.listener.MessageListener
import org.springframework.kafka.support.KafkaHeaders
import java.lang.reflect.Method
import java.time.Instant
import java.util.concurrent.TimeUnit

open class RetryMessageListenerWrapper<K, V>(
    val bean: Any,
    val method: Method
) : MessageListener<K, V> {

    companion object {
        val logger: Logger = LoggerFactory.getLogger(RetryMessageListenerWrapper::class.java)
        internal val WRAPPER_METHOD = RetryMessageListenerWrapper::class.java.getMethod("onMessage", ConsumerRecord::class.java)
    }

    override fun onMessage(consumerRecord: ConsumerRecord<K, V>?) {

        val delayTime = calculateDelayTime(consumerRecord!!.retryTimestamp())

        logger.info("Receveid message from retry topic ${consumerRecord.topic()} on retry listener. In ${delayTime} seconds the listener method will be invoked.")

        runBlocking {
            delay(delayTime, TimeUnit.SECONDS)
            method.invoke(bean, consumerRecord)
        }

    }

    private fun ConsumerRecord<K, V>.topic(consumerRecord: ConsumerRecord<K, V>) =
        String(headers().headers(KafkaHeaders.TOPIC).first().value())

    private fun ConsumerRecord<K, V>.retryTimestamp() =
        String(headers().headers(KafkaRetryPolicyErrorHandler.RETRY_TIMESTAMP_HEADER).first().value()).toLong()

    private fun calculateDelayTime(retryTimestamp: Long) =
        Instant.now().epochSecond.let { now ->
            if (retryTimestamp > now)
                retryTimestamp - now
            else 0
        }


}
