package io.zup.springframework.kafka.listener

import io.zup.springframework.kafka.helper.TestConstants
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.runBlocking
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.annotation.KafkaListener
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class Receiver<K, V> {

    private lateinit var latch: CountDownLatch

    private lateinit var retryChannel: Channel<ConsumerRecord<K, V>>

    private var mainHandler: ((ConsumerRecord<K, V>) -> Unit)? = null

    fun reset(): Receiver<K, V> =
        this.apply {
            latch = CountDownLatch(0)
            retryChannel = Channel(1)
            mainHandler = null
        }

    fun withMessageHandler(handler: (ConsumerRecord<K, V>) -> Unit): Receiver<K, V> =
        this.apply { mainHandler = handler }

    fun withInteractionCount(count: Int): Receiver<K, V> =
        this.apply { latch = CountDownLatch(count) }

    @KafkaListener(topics = [TestConstants.MAIN_TOPIC], errorHandler = "errorHandler")
    fun handle(record: ConsumerRecord<K, V>) {
        try {
            mainHandler?.invoke(record)
        } finally {
            latch.countDown()
        }
    }

    @KafkaListener(topics = [TestConstants.RETRY_TOPIC])
    fun handleRetry(record: ConsumerRecord<K, V>) {
        try {
            runBlocking {
                retryChannel.send(record)
            }
        } finally {
            latch.countDown()
        }
    }

    fun awaitRetry(): ConsumerRecord<K, V> =
        runBlocking {
            retryChannel.receive()
        }

    fun await() =
        latch.await(5, TimeUnit.SECONDS)

}