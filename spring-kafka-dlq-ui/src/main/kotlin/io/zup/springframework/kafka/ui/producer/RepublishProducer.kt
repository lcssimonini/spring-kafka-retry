package io.zup.springframework.kafka.ui.producer

import io.zup.springframework.kafka.ui.model.Message

interface RepublishProducer {

    fun send(message: Message)

}