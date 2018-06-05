package io.zup.springframework.kafka.ui.configuration

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import java.util.*

@Configuration
open class ConsumerConfiguration {

    @Value("\${spring.kafka.consumer.auto-offset-reset}")
    private lateinit var strategy : String

    @Value("\${spring.kafka.bootstrap-servers}")
    private lateinit var bootstrapServers : String

    private val groupId = UUID.randomUUID().toString()

    private fun consumerConfigs(): Map<String, Any> {
        val props = HashMap<String, Any>()

        props[ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG] = bootstrapServers
        props[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java
        props[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java
        props[ConsumerConfig.GROUP_ID_CONFIG] = groupId
        props[ConsumerConfig.AUTO_OFFSET_RESET_CONFIG] = strategy

        return props
    }

    @Bean
    open fun kafkaListenerContainerFactory(): ConcurrentKafkaListenerContainerFactory<String, String> {
        val factory = ConcurrentKafkaListenerContainerFactory<String, String>()
        factory.consumerFactory = DefaultKafkaConsumerFactory(consumerConfigs())
        factory.isBatchListener = true

        return factory
    }

}