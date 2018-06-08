package io.zup.springframework.kafka.ui.producer.impl

import io.zup.springframework.kafka.ui.model.Message
import io.zup.springframework.kafka.ui.producer.RepublishProducer
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
class RepublishProducerImpl(private val kafkaTemplate: KafkaTemplate<String, String>,
                            @Value("\${spring.kafka.dlq-ui.republish.topic}") private val republishTopic: String) : RepublishProducer {

    override fun send(message: Message) {
        kafkaTemplate.send(republishTopic, message.data)
    }

}