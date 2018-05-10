package io.zup.springframework.kafka.config

import io.zup.springframework.kafka.annotation.BackoffStrategy
import io.zup.springframework.kafka.helper.ListenerFactory
import io.zup.springframework.kafka.helper.TestConstants
import io.zup.springframework.kafka.listener.KafkaRetryPolicyErrorHandler
import io.zup.springframework.kafka.listener.Receiver
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.config.KafkaListenerContainerFactory
import org.springframework.kafka.core.*
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer
import org.springframework.kafka.listener.KafkaListenerErrorHandler
import org.springframework.kafka.test.rule.KafkaEmbedded
import org.springframework.kafka.test.utils.KafkaTestUtils
import java.time.Clock
import java.time.ZoneId


@Configuration
@EnableKafka
open class KafkaTestConfiguration {

    @Autowired
    private lateinit var kafkaEmbedded: KafkaEmbedded

    @Bean
    open fun clock(): Clock =
        Clock.fixed(TestConstants.BASE_INSTANT, ZoneId.of("UTC"))

    @Bean
    open fun producerFactory(): ProducerFactory<Int, String> =
        KafkaTestUtils.producerProps(kafkaEmbedded)
            .let { DefaultKafkaProducerFactory<Int, String>(it) }

    @Bean
    open fun kafkaTemplate(): KafkaTemplate<Int, String> =
        KafkaTemplate(producerFactory())

    @Bean
    open fun consumerFactory(): ConsumerFactory<Int, String> =
        KafkaTestUtils.consumerProps("test-group", "true", kafkaEmbedded)
            .let { DefaultKafkaConsumerFactory(it) }

    @Bean
    open fun kafkaListenerContainerFactory(): KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<Int, String>> =
        ConcurrentKafkaListenerContainerFactory<Int, String>()
            .apply {
                consumerFactory = consumerFactory()
            }

    @Bean
    open fun receiver(): Receiver<Int, String> =
        Receiver()

    @Bean
    open fun listenerFactory(): ListenerFactory<Int, String> =
        ListenerFactory(consumerFactory())

    @Bean
    open fun errorHandler(): KafkaListenerErrorHandler =
        KafkaRetryPolicyErrorHandler(
            template =  kafkaTemplate(),
            maxRetries = TestConstants.MAX_RETRIES,
            retryTopic = TestConstants.RETRY_TOPIC,
            retryInterval = TestConstants.RETRY_INTERVAL,
            dlqTopic = TestConstants.DLQ_TOPIC,
            backoffStrategy = TestConstants.BACKOFF_STRATEGY,
            clock = clock()
        )
}