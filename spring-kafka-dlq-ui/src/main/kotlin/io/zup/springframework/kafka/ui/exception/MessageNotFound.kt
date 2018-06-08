package io.zup.springframework.kafka.ui.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "No such Message for given uuid")
class MessageNotFoundException : RuntimeException()