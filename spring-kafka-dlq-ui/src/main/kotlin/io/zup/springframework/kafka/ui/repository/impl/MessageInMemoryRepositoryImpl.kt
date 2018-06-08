package io.zup.springframework.kafka.ui.repository.impl

import io.zup.springframework.kafka.ui.model.Message
import io.zup.springframework.kafka.ui.repository.MessageRepository
import org.springframework.stereotype.Component
import java.util.*
import kotlin.collections.HashMap

@Component
class MessageInMemoryRepositoryImpl : MessageRepository {

    companion object {
        private val messages = HashMap<String, Message>()
    }

    override fun get(): Map<String, Message> = messages

    override fun add(message: Message) : String {
        val uuid = UUID.randomUUID().toString()
        messages[uuid] = message

        return uuid
    }

    override fun remove(uuid: String) = messages.remove(uuid)

}