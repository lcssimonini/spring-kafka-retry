package io.zup.springframework.kafka.annotation

import io.zup.springframework.kafka.listener.KafkaRetryPolicyErrorHandler
import org.springframework.core.annotation.AnnotationUtils
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.annotation.KafkaListenerAnnotationBeanPostProcessor
import org.springframework.kafka.config.MethodKafkaListenerEndpoint
import org.springframework.kafka.core.KafkaTemplate
import java.lang.reflect.Method

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

        validateBean(bean!!)

        setErrorHandler(bean, endpoint!!)

        buildWrappedBeanTarget(endpoint, bean, adminTarget!!)
            .let { super.processListener(endpoint, kafkaListener, it.first, it.second, beanName) }
    }


    private fun setErrorHandler(bean: Any, endpoint: MethodKafkaListenerEndpoint<*, *>) =
        retryPolicyFor(bean, endpoint.method)
            ?.let { KafkaRetryPolicyErrorHandler.from(it, template) }
            ?.let { endpoint.setErrorHandler(it) }

    private fun retryPolicyFor(bean: Any, method: Method): RetryPolicy? =
        AnnotationUtils.findAnnotation(method, RetryPolicy::class.java) ?: retryPolicyForRetryKafkaListener(bean, method)

    private fun retryPolicyForRetryKafkaListener(bean: Any, method: Method): RetryPolicy? =
        AnnotationUtils.findAnnotation(method, RetryKafkaListener::class.java)?.let { retryPolicyFor(it.retryPolicyId, bean) }

    private fun retryPolicyFor(id: String, bean: Any): RetryPolicy? =
        bean.retryPolicies().find { it.id == id }


    private fun buildWrappedBeanTarget(endpoint: MethodKafkaListenerEndpoint<*, *>, bean: Any, adminTarget: Any): Pair<Any, Any> =
        asRetryMethod(adminTarget)
            ?.let { RetryMessageListenerWrapper<K, V>(bean, it) to RetryMessageListenerWrapper.WRAPPER_METHOD }
            ?.also { endpoint.method = RetryMessageListenerWrapper.WRAPPER_METHOD }
                ?: bean to adminTarget


    private fun asRetryMethod(target: Any?): Method? =
        target?.takeIf { target is Method && target.isAnnotationPresent(RetryKafkaListener::class.java) } as? Method

}
