package io.zup.springframework.kafka.annotation

import io.zup.springframework.kafka.listener.KafkaRetryPolicyErrorHandler
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.runBlocking
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.listener.MessageListener
import java.lang.reflect.Method
import java.time.Instant
import java.util.concurrent.TimeUnit

open class RetryMessageListenerWrapper<K, V>(
    val bean: Any,
    val method: Method
) : MessageListener<K, V> {

    init {
        validateKafkaRetryListenerMethod()
    }

    private fun methodName() = method.name

    private fun beanClassName() = bean.javaClass.canonicalName

    private fun validateKafkaRetryListenerMethod() {
        if (method.parameterTypes.none { clazz -> clazz == ConsumerRecord::class.java } || method.parameterCount != 1)
            throw IllegalArgumentException("Method ${methodName()} of bean class ${beanClassName()} is annotated with @RetryKafkaListenerm and must have exactly one parameter of type ${ConsumerRecord::class.java.canonicalName}")
    }

    override fun onMessage(consumerRecord: ConsumerRecord<K, V>?) {
        val retryTimestamp = retryTimestamp(consumerRecord!!)
        val delayTime = calculateDelayTime(retryTimestamp)

        println("delayTime: ${delayTime}")

        runBlocking {
            delay(delayTime, TimeUnit.SECONDS)
            method.invoke(bean, consumerRecord)
        }
    }

    private fun retryTimestamp(consumerRecord: ConsumerRecord<K, V>) =
        String(consumerRecord.headers().headers(KafkaRetryPolicyErrorHandler.RETRY_TIMESTAMP_HEADER).first().value()).toLong()

    private fun calculateDelayTime(retryTimestamp: Long) =
        Instant.now().epochSecond.let { now ->
            if (retryTimestamp > now)
                retryTimestamp - now
            else 0
        }


}
