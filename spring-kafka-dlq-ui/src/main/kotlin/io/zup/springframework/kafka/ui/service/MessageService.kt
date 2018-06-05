package io.zup.springframework.kafka.ui.service

interface MessageService {

    fun get(): List<String>

}