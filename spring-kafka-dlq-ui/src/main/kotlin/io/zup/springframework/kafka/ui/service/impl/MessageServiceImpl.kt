package io.zup.springframework.kafka.ui.service.impl

import io.zup.springframework.kafka.ui.exception.MessageNotFoundException
import io.zup.springframework.kafka.ui.model.Message
import io.zup.springframework.kafka.ui.producer.RepublishProducer
import io.zup.springframework.kafka.ui.repository.MessageRepository
import io.zup.springframework.kafka.ui.service.MessageService
import org.springframework.stereotype.Service

@Service
class MessageServiceImpl(
    private val messageRepository: MessageRepository,
    private val messageProducer: RepublishProducer
) : MessageService {

    override fun get(): Map<String, Message> = messageRepository.get()

    override fun add(message: Message) = messageRepository.add(message)

    override fun republish(uuid: String) {
        val message = messageRepository.remove(uuid) ?: throw MessageNotFoundException()
        messageProducer.send(message)
    }

}