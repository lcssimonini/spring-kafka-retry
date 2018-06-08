package io.zup.springframework.kafka.ui.integrated

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import io.zup.springframework.kafka.ui.KafkaDLQUITestApplication
import io.zup.springframework.kafka.ui.helper.TestConstants.REPUBLISH_TOPIC
import io.zup.springframework.kafka.ui.helper.TestConstants.DLQ_TOPIC
import io.zup.springframework.kafka.ui.integrated.consumer.RepublishConsumer
import org.hamcrest.Matchers.contains
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders.CONTENT_TYPE
import org.springframework.kafka.config.KafkaListenerEndpointRegistry
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.kafka.test.rule.KafkaEmbedded
import org.springframework.kafka.test.utils.ContainerTestUtils
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultMatcher
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.concurrent.TimeUnit


@RunWith(SpringRunner::class)
@SpringBootTest(
    classes = [KafkaDLQUITestApplication::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@AutoConfigureMockMvc
@EmbeddedKafka(partitions = 1, topics = [DLQ_TOPIC, REPUBLISH_TOPIC])
@DirtiesContext
class MessageIntegratedTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var kafkaTemplate: KafkaTemplate<String, String>

    @Autowired
    private lateinit var kafkaEmbedded: KafkaEmbedded

    @Autowired
    private lateinit var kafkaListenerEndpointRegistry: KafkaListenerEndpointRegistry

    @Value("\${spring.kafka.dlq-ui.topic}")
    private lateinit var dlqTopic: String

    @Autowired
    private lateinit var republishConsumer: RepublishConsumer

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Before
    fun setUp() {
        kafkaListenerEndpointRegistry.listenerContainers.forEach {
            ContainerTestUtils.waitForAssignment(it, kafkaEmbedded.partitionsPerTopic)
        }
    }

    @Test
    fun `should consume from dlq and list messages`() {
        kafkaTemplate.send(dlqTopic, "data")
        Thread.sleep(1000)

        mockMvc.perform(get("/messages"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[*].data", contains("data")))
    }

    @Test
    fun `should republish to republish topic and consume from republish topic`() {
        kafkaTemplate.send(dlqTopic, "data")

        val uuid = getFirstMessageUUID()

        mockMvc.perform(
            post("/messages/{uuid}/republish", uuid)
                .header(CONTENT_TYPE, "application/json")
        )
            .andExpect(status().isOk)

        republishConsumer.latch.await(5, TimeUnit.SECONDS)
    }

    private fun getFirstMessageUUID(): String {
        val messages = getMessages(mockMvc)
        return messages.keys.iterator().next()
    }


    private fun getMessages(mockMvc: MockMvc): Map<String, Any> {
        val json = performGet(mockMvc, "/messages", status().isOk)
        return objectMapper.readValue<Map<String, Any>>(json, object : TypeReference<Map<String, Any>>() {})
    }

    private fun performGet(mockMvc: MockMvc, url: String, resultStatus: ResultMatcher): String {
        return mockMvc.perform(
            get(url)
        )
            .andExpect(resultStatus)
            .andReturn().response.contentAsString
    }


}