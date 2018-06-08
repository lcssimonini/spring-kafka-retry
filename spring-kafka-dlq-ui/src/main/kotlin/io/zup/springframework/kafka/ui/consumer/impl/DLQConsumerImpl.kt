package io.zup.springframework.kafka.ui.consumer.impl

import io.zup.springframework.kafka.ui.consumer.DLQConsumer
import io.zup.springframework.kafka.ui.model.Message
import io.zup.springframework.kafka.ui.service.MessageService
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.stereotype.Component

@Component
class DLQConsumerImpl(private val messageService: MessageService): DLQConsumer {

    @KafkaListener(
        containerFactory = "springKafkaDLQUIListenerContainerFactory",
        topics = ["\${spring.kafka.dlq-ui.topic}"]
    )
    override fun receive(
        messages: List<String>,
        @Header(KafkaHeaders.RECEIVED_PARTITION_ID) partitions: List<Int>,
        @Header(KafkaHeaders.OFFSET) offsets: List<Long>
    ) {

        messages.forEachIndexed { i, m ->
            messageService.add(Message(partitions[i], offsets[i], m))
        }

    }
}