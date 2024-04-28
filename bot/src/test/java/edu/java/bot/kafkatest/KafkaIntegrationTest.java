package edu.java.bot.kafkatest;

import edu.common.datatypes.dtos.LinkUpdateRequest;
import edu.java.bot.api.util.UpdateHandler;
import edu.java.bot.services.TelegramBotService;
import edu.java.bot.services.TelegramBotWrapper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import java.util.List;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
@Testcontainers
@TestPropertySource(
    properties = {
        "app.kafka-settings.topics.scrapper-topic.replicas-count=1",
        "app.kafka-settings.topics.dead-letter-queue-topic.replicas-count=1",
        "spring.kafka.consumer.auto-offset-reset=earliest"
    }
)
public class KafkaIntegrationTest {
    @Container
    static final KafkaContainer kafka = new KafkaContainer(
        DockerImageName.parse("confluentinc/cp-kafka:7.3.2")
    );

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }

    @Value("${app.kafka-settings.topics.scrapper-topic.name}")
    String testTopicName;

    @MockBean
    private UpdateHandler updateHandler;

    @MockBean
    private TelegramBotService telegramBotService;

    @MockBean
    private TelegramBotWrapper telegramBotWrapper;

    @Autowired
    private KafkaTemplate<String, LinkUpdateRequest> kafkaTemplate;

    @BeforeAll
    public static void setUp() {
        kafka.start();
    }

    @AfterAll
    public static void tearDown() {
        kafka.stop();
    }

    @Test
    public void testKafkaMessageReceiving() throws Exception {
        final LinkUpdateRequest data =
            new LinkUpdateRequest(-1, "test-url", "test-description", List.of(1L, 2L, 3L));
        doNothing().when(updateHandler).handleUpdate(any());

        Thread.sleep(5000);
        kafkaTemplate.send(testTopicName, data);
        Thread.sleep(1000);

        verify(updateHandler, times(1)).handleUpdate(any());
    }
}
