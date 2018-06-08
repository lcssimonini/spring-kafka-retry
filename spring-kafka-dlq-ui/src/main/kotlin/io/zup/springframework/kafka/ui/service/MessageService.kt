package io.zup.springframework.kafka.ui.service

import io.zup.springframework.kafka.ui.model.Message

interface MessageService {

    fun get(): Map<String, Message>
    fun add(message: Message): String
    fun republish(uuid: String)

}