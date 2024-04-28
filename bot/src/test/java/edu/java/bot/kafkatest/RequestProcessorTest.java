package edu.java.bot.kafkatest;

import edu.common.datatypes.dtos.LinkUpdateRequest;
import edu.java.bot.api.util.UpdateHandler;
import edu.java.bot.configuration.ApplicationConfig;
import edu.java.bot.services.TelegramBotWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {UpdateHandler.class, TelegramBotWrapper.class})
@ExtendWith(MockitoExtension.class)
public class RequestProcessorTest {
    @Autowired
    UpdateHandler updateHandler;

    @MockBean
    ApplicationConfig applicationConfig;

    @MockBean
    KafkaTemplate<String, LinkUpdateRequest> template;

    @MockBean
    TelegramBotWrapper telegramBot;

    @Test
    public void correctRequestSendNotificationsToAllClientsTest() {
        // init
        final List<Long> clients = List.of(1L, 2L, 3L);
        final HashMap<Long, Integer> clientIdToReceivedMessage = clients
            .stream()
            .collect(Collectors.toMap(
                    k -> k,
                    v -> 0,
                    (oldValue, newValue) -> newValue,
                    HashMap::new
                ));
        final HashMap<Long, Integer> clientIdToReceivedMessageExpected = clients
            .stream()
            .collect(Collectors.toMap(
                k -> k,
                v -> 1,
                (oldValue, newValue) -> newValue,
                HashMap::new
            ));
        final LinkUpdateRequest linkUpdateRequest = new LinkUpdateRequest(
            -1, "test-url", "test-description", clients);

        // mock behaviour
        doAnswer((Answer<Void>) invocation -> {
            Long chatId = invocation.getArgument(0);
            clientIdToReceivedMessage.put(chatId, clientIdToReceivedMessage.get(chatId) + 1);
            return null;
        }).when(telegramBot).sendPlainTextMessage(anyLong(), anyString());

        // do
        updateHandler.handleUpdate(linkUpdateRequest);

        // check
        assertThat(clientIdToReceivedMessage).isEqualTo(clientIdToReceivedMessageExpected);
    }

    @Test
    public void correctRequestSendNotificationsToAllClientsInterruptedByExceptionTest() {
        // init
        final List<Long> clients = List.of(1L, 2L, 3L);
        final HashMap<Long, Integer> clientIdToReceivedMessage = clients
            .stream()
            .collect(Collectors.toMap(
                k -> k,
                v -> 0,
                (oldValue, newValue) -> newValue,
                HashMap::new
            ));
        final Map<Long, Integer> clientIdToReceivedMessageExpected = Map.of(
            1L, 1,
            2L, 1,
            3L, 0
        );
        final LinkUpdateRequest linkUpdateRequest = new LinkUpdateRequest(
            -1, "test-url", "test-description", clients);

        // mock behaviour
        doAnswer((Answer<Void>) invocation -> {
            Long chatId = invocation.getArgument(0);

            if (chatId == 3L) {
                // interrupt handling on third chat
                throw new Exception();
            }

            clientIdToReceivedMessage.put(chatId, clientIdToReceivedMessage.get(chatId) + 1);
            return null;
        }).when(telegramBot).sendPlainTextMessage(anyLong(), anyString());

        // do
        updateHandler.handleUpdate(linkUpdateRequest);

        // check
        assertThat(clientIdToReceivedMessage).isEqualTo(clientIdToReceivedMessageExpected);
    }
}
