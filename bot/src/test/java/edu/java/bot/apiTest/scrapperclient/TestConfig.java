package edu.java.bot.apiTest.scrapperclient;

import edu.java.bot.client.ScrapperClient;
import org.springframework.context.annotation.Bean;

public class TestConfig {
    @Bean("scrapperTestClient")
    public ScrapperClient scrapperTestClient() {
        return new ScrapperClient("http://localhost:8080/scrapperMock/");
    }
}
