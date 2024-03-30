package edu.java.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.common.datatypes.dtos.ApiErrorResponse;
import edu.common.datatypes.exceptions.httpresponse.BadHttpResponseException;
import edu.java.clients.GitHubClient;
import edu.java.clients.StackOverflowClient;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestClient;

@Configuration
public class ClientConfig {
    private static final Charset DEFAULT_BODY_ENCODING = StandardCharsets.UTF_8;

    @Bean("notOkResponseHandler")
    RestClient.ResponseSpec.ErrorHandler notOkResponseHandler(@Autowired ObjectMapper objectMapper) {
        return (req, resp) -> {
            ApiErrorResponse errorResponse = objectMapper.readValue(
                new String(resp.getBody().readAllBytes(), DEFAULT_BODY_ENCODING),
                ApiErrorResponse.class
            );

            HttpStatus status = HttpStatus.valueOf(resp.getStatusCode().value());

            throw new BadHttpResponseException(status, errorResponse);
        };
    }

    @Bean("gitHubClient")
    public GitHubClient gitHubClient(
        @Value("#{@githubClientSettings.timeoutInMilliseconds()}")
        int timeoutInMilliseconds,

        @Autowired
        @Qualifier("notOkResponseHandler")
        RestClient.ResponseSpec.ErrorHandler notOkResponseHandler
    ) {
        return new GitHubClient("https://api.github.com/repos/", timeoutInMilliseconds, notOkResponseHandler);
    }

    @Bean("stackOverflowClient")
    public StackOverflowClient stackoverflowClient(
        @Value("#{@stackoverflowClientSettings.timeoutInMilliseconds()}")
        int timeoutInMilliseconds,

        @Autowired
        @Qualifier("notOkResponseHandler")
        RestClient.ResponseSpec.ErrorHandler notOkResponseHandler
    ) {
        return new StackOverflowClient(timeoutInMilliseconds, notOkResponseHandler);
    }
}
