package edu.java.bot.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.common.dtos.ApiErrorResponse;
import edu.common.exceptions.IncorrectRequestException;
import edu.common.exceptions.UnexpectedResponse;
import edu.common.exceptions.httpresponse.BadHttpResponseException;
import edu.java.bot.client.ScrapperClient;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestClient;

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

            if (resp.getStatusCode() == HttpStatus.BAD_REQUEST) {
                throw new IncorrectRequestException(errorResponse.getExceptionMessage());
            } else {
                throw new UnexpectedResponse(resp.getStatusCode().value(), errorResponse.getExceptionMessage());
            }

        };
    }

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

    @Bean("scrapperClient")
    public ScrapperClient scrapperClient(
        @Autowired
        ObjectMapper mapper,

        @Autowired
        @Qualifier("defaultUnexpectedStatusHandler")
        RestClient.ResponseSpec.ErrorHandler defaultUnexpectedStatusHandler,

        @Autowired
        @Qualifier("linkManagementStatus4xxHandler")
        RestClient.ResponseSpec.ErrorHandler linkManagementStatus4xxHandler
    ) {
        return new ScrapperClient(
            mapper,
            defaultUnexpectedStatusHandler,
            linkManagementStatus4xxHandler
        );
    }
}
