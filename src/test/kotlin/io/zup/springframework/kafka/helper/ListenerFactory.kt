package io.zup.springframework.kafka.helper

import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.listener.KafkaMessageListenerContainer
import org.springframework.kafka.listener.MessageListener
import org.springframework.kafka.listener.MessageListenerContainer
import org.springframework.kafka.listener.config.ContainerProperties

class ListenerFactory<K, V>(
    val consumerFactory: ConsumerFactory<K, V>
) {

    fun listeningTo(topic: String, listener: (ConsumerRecord<K, V>) -> Unit): MessageListenerContainer =
        ContainerProperties(topic)
            .let { KafkaMessageListenerContainer(consumerFactory, it) }
            .apply {
                setupMessageListener(object : MessageListener<K, V> {
                    override fun onMessage(record: ConsumerRecord<K, V>) {
                        listener(record)
                    }
                })
            }

}