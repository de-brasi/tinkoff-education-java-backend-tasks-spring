package edu.java.scrapper.apitest;

import edu.common.exceptions.ChatIdNotExistsException;
import edu.common.exceptions.ReRegistrationException;
import edu.java.api.ChatController;
import edu.java.api.LinkController;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class ApiChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ChatController chatController;

    @MockBean
    private LinkController linkController;

    // POST: /scrapper/api/tg-chat/{id}
    @Test
    @DisplayName("Test correct chat registry in /tg-chat")
    public void test1() throws Exception {
        mockMvc.perform(
                MockMvcRequestBuilders
                    .post("/scrapper/api/tg-chat/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("")
            )
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Test correct chat registry to /tg-chat with manually occurred Exception")
    public void test2() throws Exception {
        when(chatController.handleRegistryChat(any(Long.class)))
            .thenAnswer(invocation -> {
                throw new Exception();
            });

        mockMvc.perform(
                MockMvcRequestBuilders
                    .post("/scrapper/api/tg-chat/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("")
            )
            .andExpect(status().is5xxServerError());
    }

    @Test
    @DisplayName("Test correct chat registry to /tg-chat with manually occurred ReRegistrationException")
    public void test3() throws Exception {
        when(chatController.handleRegistryChat(any(Long.class)))
            .thenAnswer(invocation -> {
                throw new ReRegistrationException();
            });

        mockMvc.perform(
                MockMvcRequestBuilders
                    .post("/scrapper/api/tg-chat/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("")
            )
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Test incorrect chat registry to /tg-chat with unexpected argument datatype")
    public void test4() throws Exception {
        mockMvc.perform(
                MockMvcRequestBuilders
                    .post("/scrapper/api/tg-chat/one")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("")
            )
            .andExpect(status().isBadRequest());
    }

    // DELETE: /scrapper/api/tg-chat/{id}
    @Test
    @DisplayName("Test correct chat delete in /tg-chat")
    public void test5() throws Exception {
        mockMvc.perform(
                MockMvcRequestBuilders
                    .delete("/scrapper/api/tg-chat/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("")
            )
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Test correct chat delete to /tg-chat with manually occurred Exception")
    public void test6() throws Exception {
        when(chatController.handleDeleteChat(any(Long.class)))
            .thenAnswer(invocation -> {
                throw new Exception();
            });

        mockMvc.perform(
                MockMvcRequestBuilders
                    .delete("/scrapper/api/tg-chat/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("")
            )
            .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("Test correct chat delete to /tg-chat with manually occurred ChatIdNotExistsException")
    public void test7() throws Exception {
        when(chatController.handleDeleteChat(any(Long.class)))
            .thenAnswer(invocation -> {
                throw new ChatIdNotExistsException();
            });

        mockMvc.perform(
                MockMvcRequestBuilders
                    .delete("/scrapper/api/tg-chat/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("")
            )
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Test incorrect chat delete to /tg-chat with unexpected argument datatype")
    public void test8() throws Exception {
        mockMvc.perform(
                MockMvcRequestBuilders
                    .delete("/scrapper/api/tg-chat/one")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("")
            )
            .andExpect(status().isBadRequest());
    }
}
