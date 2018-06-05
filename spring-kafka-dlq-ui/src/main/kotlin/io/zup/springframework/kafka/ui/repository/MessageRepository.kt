package io.zup.springframework.kafka.ui.repository

import io.zup.springframework.kafka.ui.model.Message

interface MessageRepository {

    fun get(): List<Message>

    fun add(messages: Message)

}