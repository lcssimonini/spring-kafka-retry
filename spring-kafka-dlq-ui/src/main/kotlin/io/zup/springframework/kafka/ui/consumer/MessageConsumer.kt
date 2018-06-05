package io.zup.springframework.kafka.ui.consumer

import io.zup.springframework.kafka.ui.model.Message
import io.zup.springframework.kafka.ui.service.MessageService
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.stereotype.Component

@Component
class MessageConsumer(private val messageService: MessageService) {


    @KafkaListener(topics = ["\${spring.kafka.dlq-ui.topic}"])
    private fun receive(
        messages: List<String>,
        @Header(KafkaHeaders.RECEIVED_PARTITION_ID) partitions: List<Int>,
        @Header(KafkaHeaders.OFFSET) offsets: List<Long>
    ) {

        messages.forEachIndexed { i, m ->
            messageService.add(Message(partitions[i], offsets[i], m))
        }

    }
}