# Spring Kafka Retry

Spring Kafka Retry is a opensource library under apache 2.0 license. It makes retrial of message handling a lot easier for applications built on top of spring-kafka. When a message consumption fails, it will be published to a retry topic defined by the user application for later attempts. If the max retry attempts are exceeded the message will be sent to a dead letter topic.

## Getting Started

In order to use spring kafka in your application you need to be using spring-kafka and include the Configuration KafkaRetryConfiguration in your application:

```java
@Configuration
@EnableKafka
@Import(KafkaRetryConfiguration.class)
public class AppConfig {
    
    // bean definitions
    
}
```

With SpringBootApplication you don't need to include @EnableKafka:

```java
@SpringBootApplication
@Import(KafkaRetryConfiguration.class)
public class SpringBootApp {
    
    // bean definitions

}
```

Configuring a KafkaListener with retry policy:

```java
import io.zup.springframework.kafka.annotation.RetryKafkaListener;
import io.zup.springframework.kafka.annotation.RetryPolicy;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class Listener {

    @RetryPolicy(id = "retry_policy_id", topic = "retry_topic", retries = 3, dlqTopic = "dlq_topic")
    @KafkaListener(topics = "main_topic")
    public void listen(String message) {
        // try to consume on main topic
    }

    @RetryKafkaListener(retryPolicyId = "retry_policy_id")
    @KafkaListener(topics = "retry_topic")
    public void retry(ConsumerRecord<String, String> message) {
        // retry from retry topic
    }

    @KafkaListener(topics = "dlq_topic")
    public void dlq(String message) {
        // dlq
    }

}
```


### Prerequisites

* kafka-clients 0.11.0.x, 1.0.x
* spring-kafka version 1.3.x
* jackson 2.x - required for serializing message headers used by this lib.


### Installing

* Using [Maven](https://maven.apache.org/) - Dependency Management - Include the following dependencies in pom.xml:

```xml
<properties>
    <jackson.version>2.9.5</jackson.version>
    <spring-boot.version>1.5.12.RELEASE</spring-boot.version>
    <spring-kafka.version>1.3.5.RELEASE</spring-kafka.version>
    <spring-kafka-retry.version>1.0.0.RELEASE</spring-kafka-retry.version>
</properties>

<dependencies>

    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter</artifactId>
        <version>${spring-boot.version}</version>
    </dependency>

    <dependency>
        <groupId>org.springframework.kafka</groupId>
        <artifactId>spring-kafka</artifactId>
        <version>${spring-kafka.version}</version>
    </dependency>
    
    <dependency>
        <groupId>io.zup</groupId>
        <artifactId>spring-kafka-retry</artifactId>
        <version>${spring-kafka-retry.version}</version>
    </dependency>

    <dependency>
        <groupId>com.fasterxml.jackson.core</groupId>
        <artifactId>jackson-core</artifactId>
        <version>${jackson.version}</version>
    </dependency>

    <dependency>
        <groupId>com.fasterxml.jackson.core</groupId>
        <artifactId>jackson-databind</artifactId>
        <version>${jackson.version}</version>
    </dependency>

</dependencies>
```

## Unit Testing

Setup maven test dependencies:
```xml
<properties>
    <spring-boot.version>1.5.12.RELEASE</spring-boot.version>
    <mockito.version>1.10.19</mockito.version>
    <junit.version>4.12</junit.version>
</properties>

<dependencies>

    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <version>${spring-boot.version}</version>
        <scope>test</scope>
    </dependency>

    <dependency>
        <groupId>org.springframework.kafka</groupId>
        <artifactId>spring-kafka-test</artifactId>
        <version>${spring-boot.version}</version>
        <scope>test</scope>
    </dependency>

    <dependency>
        <groupId>org.mockito</groupId>
        <artifactId>mockito-core</artifactId>
        <version>${mockito.version}</version>
        <scope>test</scope>
    </dependency>
    
    <dependency>
        <groupId>junit</groupId>
        <artifactId>junit</artifactId>
        <version>${junit.version}</version>
    </dependency>

</dependencies>
```

Unit tests using junit, mockito and KafkaEmbedded:

TestApplicationConfig.java
```java
import io.zup.springframework.kafka.config.KafkaRetryConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(KafkaRetryConfiguration.class)
public class TestApplicationConfig {
}
```

ConsumerHandler.java
```java
public interface ConsumerHandler {
    Object onListen(String message);
    Object onRetry(String message);
    Object onDlq(String message);
}
```

Consumer.java
```java
import io.zup.springframework.kafka.annotation.RetryKafkaListener;
import io.zup.springframework.kafka.annotation.RetryPolicy;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Component
public class Consumer {

    ConsumerHandler consumerHandler;

    CountDownLatch latch = new CountDownLatch(1);

    @RetryPolicy(id = "main_topic_id", topic = "retry_topic", retries = 3, dlqTopic = "dlq_topic")
    @KafkaListener(topics = "main_topic")
    public void listen(String message) {
        try {
            consumerHandler.onListen(message);
        } finally {
            latch.countDown();
        }
    }

    @RetryKafkaListener(retryPolicyId = "main_topic_id")
    @KafkaListener(topics = "retry_topic")
    public void retry(ConsumerRecord<String, String> message) {
        try {
            consumerHandler.onRetry(message.value());
        } finally {
            latch.countDown();
        }
    }

    @KafkaListener(topics = "dlq_topic")
    public void listenDLQ(String message) {
        try {
            consumerHandler.onDlq(message);
        } finally {
            latch.countDown();
        }
    }

    public void resetCount(int count) {
        latch = new CountDownLatch(count);
    }

    public boolean await() {
        try {
            return latch.await(60, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

}
```

SpringKafkaRetryTest.java
```java
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.kafka.test.rule.KafkaEmbedded;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest(classes = TestApplicationConfig.class)
@RunWith(SpringRunner.class)
@DirtiesContext
public class SpringKafkaRetryTest {

    private static String MAIN_TOPIC = "main_topic";
    private static String RETRY_TOPIC = "retry_topic";
    private static String DLQ_TOPIC = "dlq_topic";

    @ClassRule
    public static KafkaEmbedded kafkaEmbedded = new KafkaEmbedded(1, true, MAIN_TOPIC, RETRY_TOPIC, DLQ_TOPIC);

    @Autowired
    private Consumer consumer;

    private ConsumerHandler consumerHandler = Mockito.mock(ConsumerHandler.class);

    @Autowired
    private KafkaTemplate<String, String> template;

    @Autowired
    private KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;

    @Before
    public void setUp() throws Exception {
        // wait until the partitions are assigned
        for (MessageListenerContainer messageListenerContainer : kafkaListenerEndpointRegistry.getListenerContainers()) {
            ContainerTestUtils.waitForAssignment(messageListenerContainer, kafkaEmbedded.getPartitionsPerTopic());
        }
        consumer.consumerHandler = consumerHandler;
    }

    @Test
    public void shouldSucceedConsumingAtFirstAttempt() {
        String GOOD_MESSAGE = "good_message";

        // send the message
        sendAndWaitConsumer(GOOD_MESSAGE, 1);

        Mockito.verify(consumerHandler, Mockito.times(1)).onListen(GOOD_MESSAGE);
    }

    @Test
    public void shouldRetryConsumingOnceAndThenSucceedAtSecondAttempt()    {
        String BAD_MESSAGE = "bad_message";

        Mockito.when(consumerHandler.onListen(BAD_MESSAGE)).thenThrow(new RuntimeException(BAD_MESSAGE));

        sendAndWaitConsumer(BAD_MESSAGE, 2);

        Mockito.verify(consumerHandler, Mockito.times(1)).onListen(BAD_MESSAGE);
        Mockito.verify(consumerHandler, Mockito.times(1)).onRetry(Mockito.any());
    }

    @Test
    public void shouldRetryConsumingThreeTimesAndThenSendToDlq() {
        String BAD_MESSAGE = "bad_message";
        Mockito.when(consumerHandler.onListen(BAD_MESSAGE)).thenThrow(new RuntimeException(BAD_MESSAGE));
        Mockito.when(consumerHandler.onRetry(BAD_MESSAGE)).thenThrow(new RuntimeException(BAD_MESSAGE));

        sendAndWaitConsumer(BAD_MESSAGE, 5);

        Mockito.verify(consumerHandler, Mockito.times(1)).onListen(BAD_MESSAGE);
        Mockito.verify(consumerHandler, Mockito.times(3)).onRetry(BAD_MESSAGE);
        Mockito.verify(consumerHandler, Mockito.times(1)).onDlq(BAD_MESSAGE);
    }

    private void sendAndWaitConsumer(String message, int count) {
        consumer.resetCount(count);

        // send the message
        template.send(MAIN_TOPIC, message);

        Assert.assertTrue(consumer.await());
    }

}
```

## Authors

* **Diego Santos da Silveira** - [https://github.com/diegossilveira](https://github.com/diegossilveira)
* **Tafael Alves Caixeta** - [https://github.com/tafael](https://github.com/tafael)

See also the list of [contributors](https://github.com/your/project/contributors) who participated in this project.

## License

This project is licensed under the Apache License Version 2.0 - see the [LICENSE.md](LICENSE.md) file for details




