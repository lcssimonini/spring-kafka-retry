package io.zup.springframework.kafka.annotation

import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.util.ReflectionUtils
import java.lang.reflect.Method

internal class ErrorMessages {

    companion object {

        fun NOT_UNIQUE_ID(id: String?, bean: Any) =
            "Duplicated retry policy registered with id = \"${id}\" in bean class ${bean.javaClass.canonicalName}"

        fun POLICY_NOT_FOUND(id: String?, bean: Any) =
            "Retry policy with id = \"${id}\" not found in bean class ${bean.javaClass.canonicalName}"

        fun INVALID_RETRY_LISTENER_METHOD_SIGNATURE(method: Method, bean: Any) =
            "Method ${method.name} of bean class ${bean.javaClass.canonicalName} is annotated with @RetryKafkaListener and must have exactly one parameter of type ${ConsumerRecord::class.java.canonicalName}"

    }

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
            throw IllegalArgumentException(ErrorMessages.NOT_UNIQUE_ID(it, bean))
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
            throw IllegalArgumentException(ErrorMessages.POLICY_NOT_FOUND(it, bean))
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
        throw IllegalArgumentException(ErrorMessages.INVALID_RETRY_LISTENER_METHOD_SIGNATURE(method, bean))
    else true
