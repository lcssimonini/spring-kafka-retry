package io.zup.springframework.kafka.listener

import io.zup.springframework.kafka.config.KafkaTestConfiguration
import io.zup.springframework.kafka.helper.Matchers.hasHeader
import io.zup.springframework.kafka.helper.TestConstants
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.env.Environment
import org.springframework.kafka.config.KafkaListenerEndpointRegistry
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.kafka.test.rule.KafkaEmbedded
import org.springframework.kafka.test.utils.ContainerTestUtils
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.junit4.SpringRunner
import java.time.Clock

@RunWith(SpringRunner::class)
@SpringBootTest(classes = [KafkaTestConfiguration::class])
@EmbeddedKafka(partitions = 1, topics = [TestConstants.MAIN_TOPIC, TestConstants.RETRY_TOPIC, TestConstants.DLQ_TOPIC])
@DirtiesContext
class KafkaRetryPolicyErrorHandlerTest {

    @Autowired
    private lateinit var environment: Environment

    @Autowired
    private lateinit var kafkaTemplate: KafkaTemplate<Int, String>

    @Autowired
    private lateinit var kafkaEmbedded: KafkaEmbedded

    @Autowired
    private lateinit var kafkaListenerEndpointRegistry: KafkaListenerEndpointRegistry

    @Autowired
    private lateinit var receiver: Receiver<Int, String>

    @Autowired
    private lateinit var clock: Clock

    @Before
    fun setUp() {
        kafkaListenerEndpointRegistry.listenerContainers.forEach {
            ContainerTestUtils.waitForAssignment(it, kafkaEmbedded.partitionsPerTopic)
        }
    }

    @Test
    fun `should not invoke error handler when message listener succeeds`() {

        receiver
            .reset()
            .withInteractionCount(1)

        kafkaTemplate.send(TestConstants.MAIN_TOPIC, "hello")

        assertTrue(receiver.await())
    }

    @Test
    fun `should invoke message retry handler when message listener fails`() {

        receiver
            .reset()
            .withInteractionCount(2)
            .withMessageHandler {
                throw RuntimeException("Shit happens")
            }

        kafkaTemplate.send(TestConstants.MAIN_TOPIC, "hello")

        receiver.awaitRetry()
            .let {
                assertThat(it, hasHeader(KafkaRetryPolicyErrorHandler.REMAINING_RETRIES_HEADER, TestConstants.MAX_RETRIES - 1))
                assertThat(it, hasHeader(KafkaRetryPolicyErrorHandler.RETRY_TIMESTAMP_HEADER, expectedTimestamp()))
            }

        assertTrue(receiver.await())
    }

    @Test
    fun `should invoke dead letter queue handler when message retry handler fails repeatedly`() {

        receiver
            .reset()
            .withInteractionCount(1 + TestConstants.MAX_RETRIES)
            .withMessageHandler {
                throw RuntimeException("Shit happens")
            }
            .withMessageRetryHandler {
                throw RuntimeException("Shit happens even on retry")
            }

        kafkaTemplate.send(TestConstants.MAIN_TOPIC, "hello")

        repeat(TestConstants.MAX_RETRIES) { count ->
            val retryCount = count + 1
            receiver.awaitRetry()
                .let {
                    assertThat(it, hasHeader(KafkaRetryPolicyErrorHandler.REMAINING_RETRIES_HEADER, TestConstants.MAX_RETRIES - retryCount))
                    assertThat(it, hasHeader(KafkaRetryPolicyErrorHandler.RETRY_TIMESTAMP_HEADER, expectedTimestamp(retryCount)))
                }
        }

        receiver.awaitDeadLetter()
            .let { assertThat(it, hasHeader(KafkaRetryPolicyErrorHandler.REMAINING_RETRIES_HEADER, 0)) }

        assertTrue(receiver.await())
    }

    @Test
    fun `should evaluate topic name defined via properties`() {
        val name = KafkaRetryPolicyErrorHandler.resolveTopicName(environment, "\${kafka.main.topic}")
        assertEquals("main-topic", name)
    }

    @Test
    fun `should not resolve undefined topic name`() {
        val name = KafkaRetryPolicyErrorHandler.resolveTopicName(environment, "\${kafka.unresolved.topic}")
        assertEquals("\${kafka.unresolved.topic}", name)
    }

    private fun expectedTimestamp(retry: Int = 1): Long =
        TestConstants.BACKOFF_STRATEGY.calculateBackoffTimeInSeconds(clock, retry, TestConstants.RETRY_INTERVAL)

}