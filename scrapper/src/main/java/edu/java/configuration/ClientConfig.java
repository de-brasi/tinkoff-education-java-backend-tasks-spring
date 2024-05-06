package edu.java.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.common.datatypes.dtos.ApiErrorResponse;
import edu.common.datatypes.exceptions.httpresponse.BadHttpResponseException;
import edu.java.clients.GitHubClient;
import edu.java.clients.StackOverflowClient;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestClient;

@Configuration
@Slf4j
public class ClientConfig {

    private final WebClientProperties gitHubClientSettings;
    private final WebClientProperties stackOverflowClientSettings;
    private static final Charset DEFAULT_BODY_ENCODING = StandardCharsets.UTF_8;

    public ClientConfig(
        @Qualifier("githubProperties")
        WebClientProperties gitHubClientSettings,

        @Qualifier("stackoverflowProperties")
        WebClientProperties stackOverflowClientSettings
    ) {
        this.gitHubClientSettings = gitHubClientSettings;
        this.stackOverflowClientSettings = stackOverflowClientSettings;
    }

    @Bean("notOkResponseHandler")
    RestClient.ResponseSpec.ErrorHandler notOkResponseHandler(@Autowired ObjectMapper objectMapper) {
        return (req, resp) -> {
            log.info("""
                Faulty response handler:
                Request url - {}
                Request method - {}
                Response body - {}
                Response code - {}
                Response status text - {}
                """,
                req.getURI(),
                req.getMethod(),
                new String(resp.getBody().readAllBytes()),
                resp.getStatusCode(),
                resp.getStatusText()
            );

            ApiErrorResponse errorResponse = objectMapper.readValue(
                new String(resp.getBody().readAllBytes(), DEFAULT_BODY_ENCODING),
                ApiErrorResponse.class
            );

            HttpStatus status = HttpStatus.valueOf(resp.getStatusCode().value());

            throw new BadHttpResponseException(status, errorResponse);
        };
    }

    @Bean("gitHubClient")
    public GitHubClient gitHubClient(RestClient.ResponseSpec.ErrorHandler notOkResponseHandler) {
        return new GitHubClient(
            gitHubClientSettings.getBaseUrl(),
            gitHubClientSettings.getTimeoutInMilliseconds(),
            notOkResponseHandler
        );
    }

    @Bean("stackOverflowClient")
    public StackOverflowClient stackoverflowClient(RestClient.ResponseSpec.ErrorHandler notOkResponseHandler) {
        return new StackOverflowClient(
            stackOverflowClientSettings.getBaseUrl(),
            stackOverflowClientSettings.getTimeoutInMilliseconds(),
            notOkResponseHandler
        );
    }

}
