package io.zup.springframework.kafka.ui.repository.impl

import io.zup.springframework.kafka.ui.repository.MessageRepository
import org.springframework.stereotype.Component

@Component
class MessageInMemoryRepositoryImpl : MessageRepository {

    companion object {
        val messages = mutableListOf<String>()
    }

    override fun get(): List<String> = messages

    override fun add(message: String) {
        messages.add(message)
    }

}