package io.cvortex.spring.kafka.config

import io.cvortex.spring.kafka.helper.TestConstants
import io.cvortex.spring.kafka.listener.KafkaRetryPolicyErrorHandler
import io.cvortex.spring.kafka.listener.Receiver
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.config.KafkaListenerContainerFactory
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory
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
    open fun errorHandler(): KafkaListenerErrorHandler =
            KafkaRetryPolicyErrorHandler(
                    template = kafkaTemplate(),
                    maxRetries = TestConstants.MAX_RETRIES,
                    retryTopic = TestConstants.RETRY_TOPIC,
                    retryInterval = TestConstants.RETRY_INTERVAL,
                    dlqTopic = TestConstants.DLQ_TOPIC,
                    backOffStrategy = TestConstants.BACKOFF_STRATEGY,
                    clock = clock()
            )
}
