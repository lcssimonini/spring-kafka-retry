package io.zup.springframework.kafka.ui.api

import io.zup.springframework.kafka.ui.model.Message
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping

interface MessageAPI {

    @GetMapping(value = ["/messages"], produces = [(MediaType.APPLICATION_JSON_VALUE)])
    fun get(): Map<String, Message>

    @PostMapping(value = ["/messages/{uuid}/republish"], consumes = [(MediaType.APPLICATION_JSON_VALUE)])
    fun republish(uuid: String)

}