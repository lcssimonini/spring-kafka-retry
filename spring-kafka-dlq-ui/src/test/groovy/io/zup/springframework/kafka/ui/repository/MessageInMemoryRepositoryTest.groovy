package io.zup.springframework.kafka.ui.repository

import io.zup.springframework.kafka.ui.model.Message
import io.zup.springframework.kafka.ui.repository.impl.MessageInMemoryRepositoryImpl
import spock.lang.Specification

class MessageInMemoryRepositoryTest extends Specification {

    MessageRepository messageRepository = new MessageInMemoryRepositoryImpl()

    def "should add and get"() {
        given:
        Message message = new Message(1, 1L, "message")

        when:
        messageRepository.add(message)
        def result = messageRepository.get()

        then:
        result == [message]
    }

}
