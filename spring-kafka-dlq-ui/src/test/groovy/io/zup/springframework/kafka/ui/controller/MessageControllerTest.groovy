package io.zup.springframework.kafka.ui.controller

import io.zup.springframework.kafka.ui.service.MessageService
import spock.lang.Specification

class MessageControllerTest extends Specification {

    MessageService messageService = Mock(MessageService)
    MessageController messageController = new MessageController(messageService)

    def "should get messages"() {
        when:
        messageController.get()

        then:
        1 * messageService.get()
    }

}
