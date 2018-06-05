package io.zup.springframework.kafka.ui.service.impl

import io.zup.springframework.kafka.ui.model.Message
import io.zup.springframework.kafka.ui.repository.MessageRepository
import io.zup.springframework.kafka.ui.service.MessageService
import org.springframework.stereotype.Service

@Service
class MessageServiceImpl (private val messageRepository: MessageRepository) : MessageService {

    override fun get(): List<Message> = messageRepository.get()

    override fun add(message: Message) {
        messageRepository.add(message)
    }

}