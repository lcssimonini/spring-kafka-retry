package io.zup.springframework.kafka.ui.integrated.consumer

import org.springframework.kafka.annotation.KafkaListener
import java.util.concurrent.CountDownLatch


class RepublishConsumer {

    val latch = CountDownLatch(1)

    @KafkaListener(
        topics = ["\${spring.kafka.dlq-ui.republish.topic}"],
        containerFactory = "springKafkaDLQUIListenerContainerFactory"
    )
    private fun receive(messages: String) {
        latch.countDown()
    }

}