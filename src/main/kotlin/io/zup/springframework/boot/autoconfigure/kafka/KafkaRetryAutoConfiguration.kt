package io.zup.springframework.boot.autoconfigure.kafka

import io.zup.springframework.kafka.annotation.RetryAwareKafkaListenerAnnotationBeanPostProcessor
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.KafkaListenerConfigUtils
import org.springframework.kafka.config.KafkaListenerEndpointRegistry

@Configuration
open class KafkaRetryAutoConfiguration {

    @Bean(name = [KafkaListenerConfigUtils.KAFKA_LISTENER_ENDPOINT_REGISTRY_BEAN_NAME])
    open fun defaultKafkaListenerEndpointRegistry(): KafkaListenerEndpointRegistry =
        KafkaListenerEndpointRegistry()

    @Bean(KafkaListenerConfigUtils.KAFKA_LISTENER_ANNOTATION_PROCESSOR_BEAN_NAME)
    open fun retryAwareKafkaListenerAnnotationBeanPostProcessor(): RetryAwareKafkaListenerAnnotationBeanPostProcessor<*, *> =
        RetryAwareKafkaListenerAnnotationBeanPostProcessor<Any, Any>()

}