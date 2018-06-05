package io.zup.springframework.kafka.ui.controller

import io.zup.springframework.kafka.ui.api.MessageAPI
import io.zup.springframework.kafka.ui.model.Message
import io.zup.springframework.kafka.ui.service.MessageService
import org.springframework.web.bind.annotation.RestController

@RestController
class MessageController(private val messageService: MessageService) : MessageAPI {

    override fun get(): List<Message> = messageService.get()

}