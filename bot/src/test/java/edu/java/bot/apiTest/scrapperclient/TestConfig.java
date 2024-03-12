package edu.java.bot.apiTest.scrapperclient;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.java.bot.client.ScrapperClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;

public class TestConfig {
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean("scrapperTestClient")
    public ScrapperClient scrapperTestClient(@Autowired ObjectMapper mapper) {
        return new ScrapperClient("http://localhost:8080/scrapperMock/", mapper);
    }
}
