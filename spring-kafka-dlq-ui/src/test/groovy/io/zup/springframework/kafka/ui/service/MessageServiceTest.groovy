package io.zup.springframework.kafka.ui.service

import io.zup.springframework.kafka.ui.exception.MessageNotFoundException
import io.zup.springframework.kafka.ui.model.Message
import io.zup.springframework.kafka.ui.producer.RepublishProducer
import io.zup.springframework.kafka.ui.repository.MessageRepository
import io.zup.springframework.kafka.ui.service.impl.MessageServiceImpl
import spock.lang.Specification

class MessageServiceTest extends Specification {

    MessageRepository messageRepository = Mock(MessageRepository)
    RepublishProducer messageProducer = Mock(RepublishProducer)
    MessageService messageService = new MessageServiceImpl(messageRepository, messageProducer)

    def "should get messages"(){
        when:
        messageService.get()

        then:
        1 * messageRepository.get()
    }

    def "should add message"(){
        given:
        def message = new Message(1, 1L, "data")
        def uuid = "da87f1c8-dd12-4eb1-9523-903f5ba0d208"

        when:
        def result = messageService.add(message)

        then:
        1 * messageRepository.add(message) >> uuid
        result == uuid
    }

    def "should republish message"(){
        given:
        def uuid = "da87f1c8-dd12-4eb1-9523-903f5ba0d208"
        def message = new Message(1, 1L, "data")

        when:
        messageService.republish(uuid)

        then:
        1 * messageRepository.remove(uuid) >> message
        1 * messageProducer.send(message)
    }

    def "should not republish message when it does not exist"(){
        given:
        def uuid = "da87f1c8-dd12-4eb1-9523-903f5ba0d208"

        when:
        messageService.republish(uuid)

        then:
        1 * messageRepository.remove(uuid)
        0 * messageProducer.send(_)
        thrown(MessageNotFoundException)
    }

}
