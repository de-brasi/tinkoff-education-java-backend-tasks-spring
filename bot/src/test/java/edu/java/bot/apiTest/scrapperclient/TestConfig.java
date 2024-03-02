package edu.java.bot.apiTest.scrapperclient;

import edu.java.bot.client.ScrapperClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestClient;

public class TestConfig {
    @Bean("scrapperTestClient")
    public ScrapperClient scrapperTestClient() {
        return new ScrapperClient("http://localhost:8080/scrapperMock/");
    }
}
