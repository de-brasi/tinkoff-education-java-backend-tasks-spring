package edu.java.bot.apitest.scrapperclient;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.java.bot.client.ScrapperClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestClient;

public class TestConfig {
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean("scrapperTestClient")
    public ScrapperClient scrapperTestClient(
        @Autowired
        @Qualifier("notOkResponseHandler")
        RestClient.ResponseSpec.ErrorHandler notOkResponseHandler
    ) {
        return new ScrapperClient(
            "http://localhost:8080/scrapperMock/",
            notOkResponseHandler
        );
    }
}
