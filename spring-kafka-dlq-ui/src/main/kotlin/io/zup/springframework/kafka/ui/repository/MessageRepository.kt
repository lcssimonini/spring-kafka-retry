package io.zup.springframework.kafka.ui.repository

interface MessageRepository {

    fun get(): List<String>

    fun add(message: String)

}