package io.zup.springframework.kafka.ui.repository.impl

import io.zup.springframework.kafka.ui.model.Message
import io.zup.springframework.kafka.ui.repository.MessageRepository
import org.springframework.stereotype.Component

@Component
class MessageInMemoryRepositoryImpl : MessageRepository {

    companion object {
        private val messages = mutableListOf<Message>()
    }

    override fun get(): List<Message> = messages

    override fun add(message: Message) {
        messages.add(message)
    }

}