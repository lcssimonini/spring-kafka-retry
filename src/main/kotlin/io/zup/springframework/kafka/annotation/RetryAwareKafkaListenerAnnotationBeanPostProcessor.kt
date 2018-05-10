package io.zup.springframework.kafka.annotation

import io.zup.springframework.kafka.listener.KafkaRetryPolicyErrorHandler
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.core.annotation.AnnotationUtils
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.annotation.KafkaListenerAnnotationBeanPostProcessor
import org.springframework.kafka.config.MethodKafkaListenerEndpoint
import org.springframework.kafka.core.KafkaTemplate
import java.lang.reflect.Method

open class RetryAwareKafkaListenerAnnotationBeanPostProcessor<K, V>(
    val template: KafkaTemplate<K, V>
) : KafkaListenerAnnotationBeanPostProcessor<K, V>() {

    private val retryPolicyCache = RetryPolicyCache()

    override fun processListener(
        endpoint: MethodKafkaListenerEndpoint<*, *>?,
        kafkaListener: KafkaListener?,
        bean: Any?,
        adminTarget: Any?,
        beanName: String?
    ) {
        val retryPolicyBeanCache = retryPolicyCache.get(bean!!)

        var retryPolicy = AnnotationUtils.findAnnotation(endpoint!!.method, RetryPolicy::class.java)
            ?.also {
                retryPolicyBeanCache.registerRetryPolicy(it)
            }

        val retryListener = AnnotationUtils.findAnnotation(endpoint.method, RetryKafkaListener::class.java)
            ?.also {
                retryPolicy = retryPolicyBeanCache.getRetryPolicy(it.retryPolicyId)
            }

        retryPolicy
            ?.let { KafkaRetryPolicyErrorHandler.from(it, template) }
            ?.let { endpoint.setErrorHandler(it) }


        var wrappedBean = bean
        var wrappedTarget = adminTarget

        adminTarget
            ?.takeIf { retryListener != null && it is Method }
            ?.let {
                val originalMethod = it as Method
                wrappedBean = RetryMessageListenerWrapper<K, V>(bean, originalMethod)
                endpoint.method =
                        RetryMessageListenerWrapper::class.java.getMethod("onMessage", ConsumerRecord::class.java)
                wrappedTarget = endpoint.method
            }

        super.processListener(endpoint, kafkaListener, wrappedBean, wrappedTarget, beanName)

    }

    private class RetryPolicyCache {

        val beanCache: HashMap<Any, RetryPolicyBeanCache> = HashMap()

        fun get(bean: Any) =
            beanCache[bean] ?: RetryPolicyBeanCache(bean).also { beanCache[bean] = it }

    }

    private class RetryPolicyBeanCache(val bean: Any) {

        val retryPolicyCache: HashMap<String, RetryPolicy> = HashMap()

        private fun beanClassName() = bean.javaClass.canonicalName

        fun getRetryPolicy(id: String): RetryPolicy =
            retryPolicyCache.get(id)
                    ?: throw IllegalArgumentException("Retry policy with id = \"${id}\" not found in bean class ${beanClassName()}")

        fun registerRetryPolicy(retryPolicy: RetryPolicy) =
            if (!retryPolicyCache.containsKey(retryPolicy.id)) retryPolicyCache.put(retryPolicy.id, retryPolicy)
            else throw IllegalArgumentException("Duplicated retry policy registered with id = \"${retryPolicy.id}\" in bean class ${beanClassName()}")

    }

}
