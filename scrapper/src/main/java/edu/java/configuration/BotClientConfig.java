package edu.java.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.common.dtos.ApiErrorResponse;
import edu.common.exceptions.IncorrectRequestException;
import edu.common.exceptions.UnexpectedResponse;
import edu.java.clients.BotClient;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestClient;

@Configuration
public class BotClientConfig {
    private static final Charset DEFAULT_BODY_ENCODING = StandardCharsets.UTF_8;

    @Bean("endpointUpdatesStatus1xxHandler")
    RestClient.ResponseSpec.ErrorHandler endpointUpdatesStatus1xxHandler(@Autowired ObjectMapper objectMapper) {
        return (req, resp) -> {
            ApiErrorResponse errorResponse = objectMapper.readValue(
                new String(resp.getBody().readAllBytes(), DEFAULT_BODY_ENCODING),
                ApiErrorResponse.class
            );
            throw new UnexpectedResponse(
                resp.getStatusCode().value(),
                errorResponse.getExceptionMessage()
            );
        };
    }

    @Bean("endpointUpdatesStatus3xxHandler")
    RestClient.ResponseSpec.ErrorHandler endpointUpdatesStatus3xxHandler(@Autowired ObjectMapper objectMapper) {
        return (req, resp) -> {
            ApiErrorResponse errorResponse = objectMapper.readValue(
                new String(resp.getBody().readAllBytes(), DEFAULT_BODY_ENCODING),
                ApiErrorResponse.class
            );
            throw new UnexpectedResponse(
                resp.getStatusCode().value(),
                errorResponse.getExceptionMessage()
            );
        };
    }

    @Bean("endpointUpdatesStatus4xxHandler")
    RestClient.ResponseSpec.ErrorHandler endpointUpdatesStatus4xxHandler(@Autowired ObjectMapper objectMapper) {
        return (req, resp) -> {
            ApiErrorResponse errorResponse = objectMapper.readValue(
                new String(resp.getBody().readAllBytes(), DEFAULT_BODY_ENCODING),
                ApiErrorResponse.class
            );
            if (resp.getStatusCode() == HttpStatus.BAD_REQUEST) {
                throw new IncorrectRequestException(errorResponse.getExceptionMessage());
            } else {
                throw new UnexpectedResponse(resp.getStatusCode().value(), errorResponse.getExceptionMessage());
            }
        };
    }

    @Bean("endpointUpdatesStatus5xxHandler")
    RestClient.ResponseSpec.ErrorHandler endpointUpdatesStatus5xxHandler(@Autowired ObjectMapper objectMapper) {
        return (req, resp) -> {
            ApiErrorResponse errorResponse = objectMapper.readValue(
                new String(resp.getBody().readAllBytes(), DEFAULT_BODY_ENCODING),
                ApiErrorResponse.class
            );
            throw new UnexpectedResponse(
                resp.getStatusCode().value(),
                errorResponse.getExceptionMessage()
            );
        };
    }

    @Bean("botClient")
    BotClient botClient(
        @Autowired
        ObjectMapper objectMapper,

        @Autowired
        @Qualifier("endpointUpdatesStatus1xxHandler")
        RestClient.ResponseSpec.ErrorHandler endpointUpdatesStatus1xxHandler,

        @Autowired
        @Qualifier("endpointUpdatesStatus3xxHandler")
        RestClient.ResponseSpec.ErrorHandler endpointUpdatesStatus3xxHandler,

        @Autowired
        @Qualifier("endpointUpdatesStatus4xxHandler")
        RestClient.ResponseSpec.ErrorHandler endpointUpdatesStatus4xxHandler,

        @Autowired
        @Qualifier("endpointUpdatesStatus5xxHandler")
        RestClient.ResponseSpec.ErrorHandler endpointUpdatesStatus5xxHandler
    ) {
        return new BotClient(
            objectMapper,
            endpointUpdatesStatus1xxHandler,
            endpointUpdatesStatus3xxHandler,
            endpointUpdatesStatus4xxHandler,
            endpointUpdatesStatus5xxHandler
        );
    }
}
