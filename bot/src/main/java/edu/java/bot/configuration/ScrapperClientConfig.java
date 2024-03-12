package edu.java.bot.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.common.dtos.ApiErrorResponse;
import edu.common.exceptions.IncorrectRequestException;
import edu.common.exceptions.UnexpectedResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

@Configuration
public class ScrapperClientConfig {
    private static final Charset DEFAULT_BODY_ENCODING = StandardCharsets.UTF_8;

    @Bean("defaultUnexpectedStatusHandler")
    RestClient.ResponseSpec.ErrorHandler defaultUnexpectedStatusHandler(@Autowired ObjectMapper objectMapper) {
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

    @Bean("linkManagementStatus4xxHandler")
    RestClient.ResponseSpec.ErrorHandler linkManagementStatus4xxHandler(@Autowired ObjectMapper objectMapper) {
        return (req, resp) -> {
            ApiErrorResponse errorResponse = objectMapper.readValue(
                new String(resp.getBody().readAllBytes(), DEFAULT_BODY_ENCODING),
                ApiErrorResponse.class
            );

            if (resp.getStatusCode().value() == 400) {
                throw new IncorrectRequestException(errorResponse.getExceptionMessage());
            } else {
                throw new UnexpectedResponse(resp.getStatusCode().value(), errorResponse.getExceptionMessage());
            }

        };
    }
}
