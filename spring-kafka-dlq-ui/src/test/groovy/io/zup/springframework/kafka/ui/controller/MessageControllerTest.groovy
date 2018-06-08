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

    def "should republish message"() {
        given:
        def uuid = "da87f1c8-dd12-4eb1-9523-903f5ba0d208"

        when:
        messageController.republish(uuid)

        then:
        1 * messageService.republish(uuid)
    }

}
