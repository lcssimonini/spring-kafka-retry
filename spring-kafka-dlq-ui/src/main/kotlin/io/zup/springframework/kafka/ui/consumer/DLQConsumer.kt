package io.zup.springframework.kafka.ui.consumer

interface DLQConsumer {

    fun receive(
        messages: List<String>,
        partitions: List<Int>,
        offsets: List<Long>
    )

}