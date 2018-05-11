package io.zup.springframework.kafka.annotation

import org.apache.kafka.clients.consumer.ConsumerRecord

@Suppress("UNUSED_PARAMETER")
class ValidBeanClass {
    @RetryPolicy(id = "id_1", retries = 2, topic = "retry", dlqTopic = "dlq")
    fun methodOne(consumerRecord: ConsumerRecord<String, String>?) { }

    @RetryPolicy(id = "id_2", retries = 2, topic = "retry_2", dlqTopic = "dlq")
    fun methodTwo(consumerRecord: ConsumerRecord<String, String>?) { }

    @RetryKafkaListener(retryPolicyId = "id_1")
    fun retryOne(consumerRecord: ConsumerRecord<String, String>?) { }

    @RetryKafkaListener(retryPolicyId = "id_2")
    fun retryTwo(consumerRecord: ConsumerRecord<String, String>?) { }

}

@Suppress("UNUSED_PARAMETER")
class DuplicatedIdsClass {
    @RetryPolicy(id = "same_id", retries = 2, topic = "retry", dlqTopic = "dlq")
    fun methodOne(consumerRecord: ConsumerRecord<String, String>?) { }

    @RetryPolicy(id = "same_id", retries = 2, topic = "retry_2", dlqTopic = "dlq")
    fun methodTwo(consumerRecord: ConsumerRecord<String, String>?) { }
}

@Suppress("UNUSED_PARAMETER")
class MatchingRetryPolicyClass {
    @RetryPolicy(id = "matching_id", retries = 2, topic = "retry", dlqTopic = "dlq")
    fun methodOne(consumerRecord: ConsumerRecord<String, String>?) { }

    @RetryKafkaListener(retryPolicyId = "matching_id")
    fun methodTwo(consumerRecord: ConsumerRecord<String, String>?) { }
}

@Suppress("UNUSED_PARAMETER")
class MissingRetryPolicyClass {
    @RetryKafkaListener(retryPolicyId = "missing_id")
    fun methodTwo(consumerRecord: ConsumerRecord<String, String>?) { }
}

@Suppress("UNUSED_PARAMETER")
class InvalidMethodSignatureBeanClassOne() {

    @RetryKafkaListener(retryPolicyId = "doenst_matter")
    fun validMethodSignature(consumerRecord: ConsumerRecord<String, String>?) { }

    @RetryKafkaListener(retryPolicyId = "doenst_matter")
    fun invalidSignatureMethodOne(record: String) { }

}

@Suppress("UNUSED_PARAMETER")
class InvalidMethodSignatureBeanClassTwo() {

    @RetryKafkaListener(retryPolicyId = "doenst_matter")
    fun invalidSignatureMethodTwo(consumerRecord: ConsumerRecord<String, String>?, invalidArg: Int) { }

}

@Suppress("UNUSED_PARAMETER")
class ValidMethodSignatureBeanClass() {

    @RetryKafkaListener(retryPolicyId = "doenst_matter")
    fun validMethodSignatureOne(consumerRecord: ConsumerRecord<String, String>?) { }

    @RetryKafkaListener(retryPolicyId = "doenst_matter")
    fun validMethodSignatureTwo(argNameWhatever: ConsumerRecord<Any, Any>?) { }

}
