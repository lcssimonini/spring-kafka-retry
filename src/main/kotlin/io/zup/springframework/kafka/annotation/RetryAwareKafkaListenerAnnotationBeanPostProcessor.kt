package io.zup.springframework.kafka.annotation

import io.zup.springframework.kafka.listener.KafkaRetryPolicyErrorHandler
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.annotation.AnnotationUtils
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.annotation.KafkaListenerAnnotationBeanPostProcessor
import org.springframework.kafka.config.MethodKafkaListenerEndpoint
import org.springframework.kafka.core.KafkaTemplate

open class RetryAwareKafkaListenerAnnotationBeanPostProcessor<K, V>(
    val template: KafkaTemplate<K, V>
) : KafkaListenerAnnotationBeanPostProcessor<K, V>() {

    override fun processListener(
        endpoint: MethodKafkaListenerEndpoint<*, *>?,
        kafkaListener: KafkaListener?,
        bean: Any?,
        adminTarget: Any?,
        beanName: String?
    ) {

        val retryPolicy = AnnotationUtils.findAnnotation(endpoint?.method, RetryPolicy::class.java)
        // TODO: set error handler on retry listener whenever RetryKafkaListener annotation is present
        val retryListener = AnnotationUtils.findAnnotation(endpoint?.method, RetryKafkaListener::class.java)

        retryPolicy
            ?.let { KafkaRetryPolicyErrorHandler.from(it, template) }
            ?.let { endpoint?.setErrorHandler(it) }

        super.processListener(endpoint, kafkaListener, bean, adminTarget, beanName)

    }


}