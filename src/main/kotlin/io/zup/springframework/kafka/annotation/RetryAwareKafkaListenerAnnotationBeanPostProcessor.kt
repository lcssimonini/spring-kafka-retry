package io.zup.springframework.kafka.annotation

import io.zup.springframework.kafka.listener.KafkaRetryPolicyErrorHandler
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.core.annotation.AnnotationUtils
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.annotation.KafkaListenerAnnotationBeanPostProcessor
import org.springframework.kafka.config.MethodKafkaListenerEndpoint
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.util.ReflectionUtils
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

        retryPolicyFor(bean!!, endpoint!!.method)
            ?.let { KafkaRetryPolicyErrorHandler.from(it, template) }
            ?.let { endpoint.setErrorHandler(it) }

        var wrappedBean = bean
        var wrappedTarget = adminTarget

        asRetryMethod(adminTarget)
            ?.let {
                val originalMethod = it as Method
                wrappedBean = RetryMessageListenerWrapper<K, V>(bean, originalMethod)
                endpoint.method =
                        RetryMessageListenerWrapper::class.java.getMethod("onMessage", ConsumerRecord::class.java)
                wrappedTarget = endpoint.method
            }

        super.processListener(endpoint, kafkaListener, wrappedBean, wrappedTarget, beanName)
    }

    private fun retryPolicyFor(bean: Any, method: Method): RetryPolicy? =
            AnnotationUtils.findAnnotation(method, RetryPolicy::class.java) ?: retryPolicyForRetryKafkaListener(bean, method)


    private fun retryPolicyForRetryKafkaListener(bean: Any, method: Method): RetryPolicy? =
        AnnotationUtils.findAnnotation(method, RetryKafkaListener::class.java)
            ?.let { retryPolicyFor(it.retryPolicyId, bean) }

    private fun retryPolicyFor(id: String, bean: Any): RetryPolicy? =
        ReflectionUtils.getAllDeclaredMethods(bean.javaClass)
            .map { AnnotationUtils.findAnnotation(it, RetryPolicy::class.java) }
            .find { it.id == id }

    private fun asRetryMethod(target: Any?): Method? =
        target?.takeIf { target is Method && target.isAnnotationPresent(RetryKafkaListener::class.java) } as? Method

//        fun getRetryPolicy(id: String): RetryPolicy =
//            retryPolicyCache.get(id)
//                    ?: throw IllegalArgumentException("Retry policy with id = \"${id}\" not found in bean class ${beanClassName()}")
//
//        fun registerRetryPolicy(retryPolicy: RetryPolicy) =
//            if (!retryPolicyCache.containsKey(retryPolicy.id)) retryPolicyCache.put(retryPolicy.id, retryPolicy)
//            else throw IllegalArgumentException("Duplicated retry policy registered with id = \"${retryPolicy.id}\" in bean class ${beanClassName()}")

}
