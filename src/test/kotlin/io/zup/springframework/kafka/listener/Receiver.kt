package io.zup.springframework.kafka.listener

import io.zup.springframework.kafka.helper.TestConstants
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.runBlocking
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.annotation.KafkaListener
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

typealias Handler<K, V> = (ConsumerRecord<K, V>) -> Unit

class Receiver<K, V> {

    private lateinit var latch: CountDownLatch
    private lateinit var retryChannel: Channel<ConsumerRecord<K, V>>
    private lateinit var dlqChannel: Channel<ConsumerRecord<K, V>>
    private var mainHandler: Handler<K, V>? = null
    private var retryHandler: Handler<K, V>? = null

    fun reset(): Receiver<K, V> =
        this.apply {
            latch = CountDownLatch(0)
            retryChannel = Channel(TestConstants.MAX_RETRIES)
            dlqChannel = Channel(1)
            mainHandler = null
            retryHandler = null
        }

    fun withInteractionCount(count: Int): Receiver<K, V> =
        this.apply { latch = CountDownLatch(count) }

    fun withMessageHandler(handler: Handler<K, V>): Receiver<K, V> =
        this.apply { mainHandler = handler }

    fun withMessageRetryHandler(handler: Handler<K, V>): Receiver<K, V> =
        this.apply { retryHandler = handler }

    @KafkaListener(topics = [TestConstants.MAIN_TOPIC], errorHandler = "errorHandler")
    fun handle(record: ConsumerRecord<K, V>) {
        try {
            mainHandler?.invoke(record)
        } finally {
            latch.countDown()
        }
    }

    @KafkaListener(topics = [TestConstants.RETRY_TOPIC], errorHandler = "errorHandler")
    fun handleRetry(record: ConsumerRecord<K, V>) {
        try {
            retryHandler?.invoke(record)
        } finally {
            runBlocking {
                retryChannel.send(record)
            }
            latch.countDown()
        }
    }

    @KafkaListener(topics = [TestConstants.DLQ_TOPIC])
    fun handleDlq(record: ConsumerRecord<K, V>) {
        runBlocking {
            dlqChannel.send(record)
        }
        latch.countDown()
    }

    fun awaitRetry(): ConsumerRecord<K, V> =
        runBlocking {
            retryChannel.receive()
        }

    fun awaitDeadLetter(): ConsumerRecord<K, V> =
        runBlocking {
            dlqChannel.receive()
        }

    fun await() =
        latch.await(5, TimeUnit.SECONDS)

}