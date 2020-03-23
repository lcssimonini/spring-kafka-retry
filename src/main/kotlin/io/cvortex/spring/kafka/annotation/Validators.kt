package io.cvortex.spring.kafka.annotation

import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.util.ReflectionUtils
import java.lang.reflect.Method

internal object ErrorMessages {

    fun notUniqueId(id: String?, bean: Any) =
        "Duplicated retry policy registered with id = \"${id}\" in bean class ${bean.javaClass.canonicalName}"

    fun policyNotFound(id: String?, bean: Any) =
        "Retry policy with id = \"${id}\" not found in bean class ${bean.javaClass.canonicalName}"

    fun invalidRetryListenerMethodSignature(method: Method, bean: Any) =
        "Method ${method.name} of bean class ${bean.javaClass.canonicalName} is annotated with @RetryKafkaListener and must have exactly one parameter of type ${ConsumerRecord::class.java.canonicalName}"

}

internal fun validateBean(bean: Any) =
    validateUniqueRetryPolicyId(bean) &&
            validateRetryListenersAreMatchingRetryPolicies(bean) &&
            validateKafkaRetryListenerMethods(bean)


internal fun validateUniqueRetryPolicyId(bean: Any): Boolean =
    bean.retryPolicies()
        .duplicatedIds()
        .takeIf { it.isNotEmpty() }
        ?.first()
        ?.let {
            throw IllegalArgumentException(ErrorMessages.notUniqueId(it, bean))
        } ?: true


private fun List<RetryPolicy>.duplicatedIds(): Set<String> =
    this.groupingBy { it.id }
        .eachCount()
        .filter { it.value > 1 }
        .keys

internal fun validateRetryListenersAreMatchingRetryPolicies(bean: Any): Boolean =
    listenersIdsWithoutCorrespondingRetryPolicyId(bean.retryListeners(), bean.retryPolicies())
        .takeIf { it.isNotEmpty() }
        ?.first()
        ?.let {
            throw IllegalArgumentException(ErrorMessages.policyNotFound(it, bean))
        } ?: true


private fun listenersIdsWithoutCorrespondingRetryPolicyId(
        retryListeners: List<RetryKafkaListener>,
        retryPolicies: List<RetryPolicy>
): List<String> =
    retryListeners.filter { retryListener ->
        retryPolicies.none { retryPolicy ->
            retryPolicy.id == retryListener.retryPolicyId
        }
    }.map { it.retryPolicyId }


internal fun validateKafkaRetryListenerMethods(bean: Any): Boolean =
    ReflectionUtils.getAllDeclaredMethods(bean.javaClass)
        .filter { it.isAnnotationPresent(RetryKafkaListener::class.java) }
        .all { validateKafkaRetryListenerMethod(bean, it) }

private fun validateKafkaRetryListenerMethod(bean: Any, method: Method): Boolean =
    if (method.parameterTypes.none { clazz -> clazz == ConsumerRecord::class.java } || method.parameterCount != 1)
        throw IllegalArgumentException(ErrorMessages.invalidRetryListenerMethodSignature(method, bean))
    else true
