package io.zup.springframework.kafka.ui.service

import io.zup.springframework.kafka.ui.repository.MessageRepository
import io.zup.springframework.kafka.ui.service.impl.MessageServiceImpl
import spock.lang.Specification

class MessageServiceTest extends Specification {

    MessageRepository messageRepository = Mock(MessageRepository)
    MessageService messageService = new MessageServiceImpl(messageRepository)

    def "should get messages"(){
        when:
        messageService.get()

        then:
        1 * messageRepository.get()
    }

}
