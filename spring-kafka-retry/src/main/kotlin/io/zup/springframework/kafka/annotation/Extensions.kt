package io.zup.springframework.kafka.annotation

import org.springframework.core.annotation.AnnotationUtils
import org.springframework.util.ReflectionUtils

internal fun Any.retryPolicies() =
    ReflectionUtils.getAllDeclaredMethods(javaClass)
        .mapNotNull { AnnotationUtils.findAnnotation(it, RetryPolicy::class.java) }

internal fun Any.retryListeners() =
    ReflectionUtils.getAllDeclaredMethods(javaClass)
        .mapNotNull { AnnotationUtils.findAnnotation(it, RetryKafkaListener::class.java) }
