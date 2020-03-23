package io.cvortex.spring.kafka.config

import io.cvortex.spring.kafka.annotation.RetryAwareKafkaListenerAnnotationBeanPostProcessor
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.kafka.config.KafkaListenerConfigUtils
import org.springframework.kafka.config.KafkaListenerEndpointRegistry
import org.springframework.kafka.core.KafkaTemplate

@Configuration
open class KafkaRetryConfiguration {

    @Autowired
    private lateinit var kafkaTemplate: KafkaTemplate<Any, Any>

    @Autowired
    private lateinit var environment: Environment

    @Bean(KafkaListenerConfigUtils.KAFKA_LISTENER_ENDPOINT_REGISTRY_BEAN_NAME)
    open fun defaultKafkaListenerEndpointRegistry(): KafkaListenerEndpointRegistry =
        KafkaListenerEndpointRegistry()

    @Bean(KafkaListenerConfigUtils.KAFKA_LISTENER_ANNOTATION_PROCESSOR_BEAN_NAME)
    open fun retryAwareKafkaListenerAnnotationBeanPostProcessor(): RetryAwareKafkaListenerAnnotationBeanPostProcessor<*, *> =
            RetryAwareKafkaListenerAnnotationBeanPostProcessor(kafkaTemplate, environment)

}