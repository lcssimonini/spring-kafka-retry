package io.zup.springframework.kafka.ui.model

data class Message(val partition : Int, val offset : Long, val data : String)