package io.zup.springframework.kafka.ui.repository

import io.zup.springframework.kafka.ui.model.Message

interface MessageRepository {

    fun get() : Map<String, Message>

    fun add(messages: Message) : String

    fun remove(uuid: String) : Message?

}