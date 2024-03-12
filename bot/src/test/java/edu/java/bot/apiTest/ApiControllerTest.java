package edu.java.bot.apiTest;

import edu.common.dtos.LinkUpdateRequest;
import edu.common.exceptions.ChatIdNotExistsException;
import edu.java.bot.api.UpdateController;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.Mockito.when;


@SpringBootTest
@AutoConfigureMockMvc
public class ApiControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UpdateController controllers;

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

        when(controllers.handleUpdateRequest(any(LinkUpdateRequest.class)))
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
