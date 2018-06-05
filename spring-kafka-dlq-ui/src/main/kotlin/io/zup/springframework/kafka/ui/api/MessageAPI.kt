package io.zup.springframework.kafka.ui.api

import io.zup.springframework.kafka.ui.model.Message
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping

interface MessageAPI {

    @GetMapping(value = ["/messages"], produces = [(MediaType.APPLICATION_JSON_VALUE)])
    fun get(): List<Message>

}