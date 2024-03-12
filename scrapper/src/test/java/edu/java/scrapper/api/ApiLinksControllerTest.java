package edu.java.scrapper.api;

import edu.common.exceptions.LinkNotExistsException;
import edu.java.api.ChatController;
import edu.java.api.LinksController;
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
public class ApiLinksControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ChatController chatController;

    @MockBean
    private LinksController linksController;

    // GET: /scrapper/api/links
    @Test
    @DisplayName("Test correct links get in /links")
    public void test9() throws Exception {
        mockMvc.perform(
                MockMvcRequestBuilders
                    .get("/scrapper/api/links")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Accept", "*/*")
                    .header("Accept-Encoding", "gzip, deflate, br")
                    .header("Tg-Chat-Id", "1")
            )
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Test correct link get to /links with manually occurred Exception")
    public void test10() throws Exception {
        when(linksController.handleGetLinks(any(Long.class)))
            .thenAnswer(invocation -> {
                throw new Exception();
            });

        mockMvc.perform(
                MockMvcRequestBuilders
                    .get("/scrapper/api/links")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Tg-Chat-Id", "1")
                    .content("")
            )
            .andExpect(status().is5xxServerError());
    }

    @Test
    @DisplayName("Test correct link get to /links with manually occurred LinkNotExistsException")
    public void test11() throws Exception {
        when(linksController.handleGetLinks(any(Long.class)))
            .thenAnswer(invocation -> {
                throw new LinkNotExistsException();
            });

        mockMvc.perform(
                MockMvcRequestBuilders
                    .get("/scrapper/api/links")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Tg-Chat-Id", "1")
                    .content("")
            )
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Test incorrect link get to /links without required header")
    public void test12() throws Exception {
        mockMvc.perform(
                MockMvcRequestBuilders
                    .get("/scrapper/api/links")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("")
            )
            .andExpect(status().isBadRequest());
    }

    // DELETE: /scrapper/api/links
    @Test
    @DisplayName("Test correct links delete in /links")
    public void test13() throws Exception {
        mockMvc.perform(
                MockMvcRequestBuilders
                    .delete("/scrapper/api/links")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Accept", "*/*")
                    .header("Accept-Encoding", "gzip, deflate, br")
                    .header("Tg-Chat-Id", "1")
            )
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Test correct link delete to /links with manually occurred Exception")
    public void test14() throws Exception {
        when(linksController.handleDeleteLinks(any(Long.class)))
            .thenAnswer(invocation -> {
                throw new Exception();
            });

        mockMvc.perform(
                MockMvcRequestBuilders
                    .delete("/scrapper/api/links")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Tg-Chat-Id", "1")
                    .content("")
            )
            .andExpect(status().is5xxServerError());
    }

    @Test
    @DisplayName("Test correct link delete to /links with manually occurred LinkNotExistsException")
    public void test15() throws Exception {
        when(linksController.handleDeleteLinks(any(Long.class)))
            .thenAnswer(invocation -> {
                throw new LinkNotExistsException();
            });

        mockMvc.perform(
                MockMvcRequestBuilders
                    .delete("/scrapper/api/links")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Tg-Chat-Id", "1")
                    .content("")
            )
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Test incorrect link delete to /links without required header")
    public void test16() throws Exception {
        mockMvc.perform(
                MockMvcRequestBuilders
                    .delete("/scrapper/api/links")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("")
            )
            .andExpect(status().isBadRequest());
    }

}
