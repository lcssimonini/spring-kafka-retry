package io.zup.springframework.kafka.annotation

import org.apache.kafka.clients.consumer.ConsumerRecord
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException

class RetryKafkaListenerValidatorsTest {

    @Rule
    @JvmField
    val expectedException: ExpectedException = ExpectedException.none()

    @Test
    fun `should pass validation for RetryPolicies with different ids`() {
        val beanInstance = ValidBeanClass()
        assertTrue(validateUniqueRetryPolicyId(beanInstance))
    }

    @Test
    fun `should throw validation error for RetryPolicies with duplicated ids`() {
        val beanInstance = DuplicatedIdsClass()

        expectedException.expect(IllegalArgumentException::class.java)
        expectedException.expectMessage(ErrorMessages.notUniqueId("same_id", beanInstance))

        validateUniqueRetryPolicyId(beanInstance)
    }

    @Test
    fun `should pass validation for a RetryListener with matching RetryPolicy`() {
        val beanInstance = MatchingRetryPolicyClass()
        assertTrue(validateRetryListenersAreMatchingRetryPolicies(beanInstance))
    }

    @Test
    fun `should throw validation error for a RetryListener with missing RetryPolicy`() {
        val beanInstance = MissingRetryPolicyClass()

        expectedException.expect(IllegalArgumentException::class.java)
        expectedException.expectMessage(ErrorMessages.policyNotFound("missing_id", beanInstance))

        validateRetryListenersAreMatchingRetryPolicies(beanInstance)
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

        expectedException.expect(IllegalArgumentException::class.java)
        expectedException.expectMessage(ErrorMessages.invalidRetryListenerMethodSignature(invalidMethodOne, beanInstanceOne))

        validateKafkaRetryListenerMethods(beanInstanceOne)
    }

    @Test
    fun `should throw validation error for invalid RetryListener method invalid args count`() {
        val beanInstanceTwo = InvalidMethodSignatureBeanClassTwo()
        val invalidMethodTwo = InvalidMethodSignatureBeanClassTwo::class.java.getDeclaredMethod("invalidSignatureMethodTwo", ConsumerRecord::class.java, Int::class.java)

        expectedException.expect(IllegalArgumentException::class.java)
        expectedException.expectMessage(ErrorMessages.invalidRetryListenerMethodSignature(invalidMethodTwo, beanInstanceTwo))

        validateKafkaRetryListenerMethods(beanInstanceTwo)
    }

    @Test
    fun `should pass all validations altogether`() {
        val beanInstance = ValidBeanClass()
        assertTrue(validateBean(beanInstance))
    }

}
