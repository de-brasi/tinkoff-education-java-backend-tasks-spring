package edu.java.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.common.datatypes.dtos.ApiErrorResponse;
import edu.common.datatypes.exceptions.IncorrectRequestException;
import edu.common.datatypes.exceptions.UnexpectedResponse;
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
        @Qualifier("notOkResponseHandler")
        RestClient.ResponseSpec.ErrorHandler notOkResponseHandler
    ) {
        return new BotClient(notOkResponseHandler);
    }
}
