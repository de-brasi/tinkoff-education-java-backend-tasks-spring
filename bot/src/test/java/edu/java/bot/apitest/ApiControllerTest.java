package edu.java.bot.apitest;

import edu.common.datatypes.dtos.LinkUpdateRequest;
import edu.common.datatypes.exceptions.ChatIdNotExistsException;
import edu.java.bot.api.UpdateController;
import edu.java.bot.services.TelegramBotService;
import edu.java.bot.services.TelegramBotWrapper;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.Mockito.when;

@WebMvcTest(UpdateController.class)
public class ApiControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UpdateController controller;

    @MockBean
    private TelegramBotWrapper telegramBotWrapper;

    @MockBean
    private TelegramBotService telegramBotService;

    @Test
    @DisplayName("Test correct (stub) request")
    public void test1() throws Exception {
        final String jsonRequest = "{\"id\": 0,\"url\": \"test\",\"description\": \"test\",\"tgChatIds\": null}";

        mockMvc.perform(
                MockMvcRequestBuilders
                    .post("/bot/api/updates")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonRequest)
            )
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Test empty body request")
    public void test2() throws Exception {
        final String jsonRequest = "";

        mockMvc.perform(
                MockMvcRequestBuilders
                    .post("/bot/api/updates")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonRequest)
            )
            .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("Test response when ChatIdNotExistsException occurs")
    public void test3() throws Exception {
        final String jsonRequest = "{\"id\": 0,\"url\": \"test\",\"description\": \"test\",\"tgChatIds\": null}";

        final String exceptionMessage = "test message";

        when(controller.handleUpdateRequest(any(LinkUpdateRequest.class), any(HttpServletRequest.class)))
            .thenAnswer(invocation -> {
                throw new ChatIdNotExistsException(exceptionMessage);
            });

        mockMvc.perform(
                MockMvcRequestBuilders
                    .post("/bot/api/updates")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonRequest)
            )
            .andExpect(status().is4xxClientError());
    }
}
