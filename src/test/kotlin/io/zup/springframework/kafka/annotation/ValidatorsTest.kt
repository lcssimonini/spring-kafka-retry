package io.zup.springframework.kafka.annotation

import io.zup.springframework.kafka.helper.assertFails
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.junit.Assert.assertTrue
import org.junit.Test

class RetryKafkaListenerValidatorsTest {

    @Test
    fun `should pass validation for RetryPolicies with different ids`() {
        val beanInstance = ValidBeanClass()
        assertTrue(validateUniqueRetryPolicyId(beanInstance))
    }

    @Test
    fun `should throw validation error for RetryPolicies with duplicated ids`() {
        val beanInstance = DuplicatedIdsClass()
        assertFails(
            IllegalArgumentException::class.java,
            ErrorMessages.NOT_UNIQUE_ID("same_id", beanInstance)
        ) {
            validateUniqueRetryPolicyId(beanInstance)
        }
    }

    @Test
    fun `should pass validation for a RetryListener with matching RetryPolicy`() {
        val beanInstance = MatchingRetryPolicyClass()
        assertTrue(validateRetryListenersAreMatchingRetryPolicies(beanInstance))
    }

    @Test
    fun `should throw validation error for a RetryListener with missing RetryPolicy`() {
        val beanInstance = MissingRetryPolicyClass()
        assertFails(
            IllegalArgumentException::class.java,
            ErrorMessages.POLICY_NOT_FOUND("missing_id", beanInstance)
        ) {
            validateRetryListenersAreMatchingRetryPolicies(beanInstance)
        }
    }

    @Test
    fun `should pass validation for valid RetryListener methods`() {
        val beanInstance = ValidMethodSignatureBeanClass()
        assertTrue(validateKafkaRetryListenerMethods(beanInstance))
    }

    @Test
    fun `should throw validation error for invalid RetryListener method with invalid arg type`() {
        val beanInstanceOne = InvalidMethodSignatureBeanClassOne()
        val invalidMethodOne = InvalidMethodSignatureBeanClassOne::class.java.getDeclaredMethod("invalidSignatureMethodOne", String::class.java)
        assertFails(
            IllegalArgumentException::class.java,
            ErrorMessages.INVALID_RETRY_LISTENER_METHOD_SIGNATURE(
                invalidMethodOne,
                beanInstanceOne
            )
        ) {
            validateKafkaRetryListenerMethods(beanInstanceOne)
        }
    }

    @Test
    fun `should throw validation error for invalid RetryListener method invalid args count`() {
        val beanInstanceTwo = InvalidMethodSignatureBeanClassTwo()
        val invalidMethodTwo = InvalidMethodSignatureBeanClassTwo::class.java.getDeclaredMethod("invalidSignatureMethodTwo", ConsumerRecord::class.java, Int::class.java)
        assertFails(
            IllegalArgumentException::class.java,
            ErrorMessages.INVALID_RETRY_LISTENER_METHOD_SIGNATURE(
                invalidMethodTwo,
                beanInstanceTwo
            )
        ) {
            validateKafkaRetryListenerMethods(beanInstanceTwo)
        }
    }

    @Test
    fun `should pass all validations altogether`() {
        val beanInstance = ValidBeanClass()
        assertTrue(validateBean(beanInstance))
    }

}
