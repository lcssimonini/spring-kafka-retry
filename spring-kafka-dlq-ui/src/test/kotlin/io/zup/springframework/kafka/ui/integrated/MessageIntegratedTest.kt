package io.zup.springframework.kafka.ui.integrated

import io.zup.springframework.kafka.ui.KafkaDLQUITestApplication
import io.zup.springframework.kafka.ui.helper.TestConstants.TOPIC
import org.hamcrest.Matchers.`is`
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.kafka.config.KafkaListenerEndpointRegistry
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.kafka.test.rule.KafkaEmbedded
import org.springframework.kafka.test.utils.ContainerTestUtils
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status


@RunWith(SpringRunner::class)
@SpringBootTest(
    classes = [KafkaDLQUITestApplication::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@AutoConfigureMockMvc
@EmbeddedKafka(partitions = 1, topics = [TOPIC])
class MessageIntegratedTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var kafkaTemplate : KafkaTemplate<String, String>

    @Autowired
    lateinit var kafkaEmbedded: KafkaEmbedded

    @Autowired
    lateinit var kafkaListenerEndpointRegistry: KafkaListenerEndpointRegistry

    @Value("\${spring.kafka.dlq-ui.topic}")
    lateinit var topic : String

    @Before
    fun setUp() {
        kafkaListenerEndpointRegistry.listenerContainers.forEach {
            ContainerTestUtils.waitForAssignment(it, kafkaEmbedded.partitionsPerTopic)
        }
    }

    @Test
    fun `should consume and list messages`() {
        kafkaTemplate.send(topic, "data")
        Thread.sleep(3000)

        mockMvc.perform(get("/messages"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].data", `is`("data")))
    }

}