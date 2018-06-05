package io.zup.springframework.kafka.ui.service

import io.zup.springframework.kafka.ui.model.Message

interface MessageService {

    fun get(): List<Message>
    fun add(message : Message)

}