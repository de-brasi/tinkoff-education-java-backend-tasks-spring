package edu.java.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.common.dtos.ApiErrorResponse;
import edu.common.exceptions.IncorrectRequestException;
import edu.common.exceptions.UnexpectedResponse;
import edu.java.clients.BotClient;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;

@Configuration
public class BotClientConfig {
    private static final Charset DEFAULT_BODY_ENCODING = StandardCharsets.UTF_8;

    @Bean("responseErrorHandler")
    ResponseErrorHandler responseErrorHandler(ObjectMapper objectMapper) {
        return new BotClientResponseErrorHandler(objectMapper);
    }

    @Bean("botClient")
    BotClient botClient(ResponseErrorHandler errorHandler) {
        return new BotClient(errorHandler);
    }

    private record BotClientResponseErrorHandler(ObjectMapper objectMapper) implements ResponseErrorHandler {

        @Override
            public boolean hasError(@NotNull ClientHttpResponse response) throws IOException {
                return !response.getStatusCode().is2xxSuccessful();
            }

            @Override
            public void handleError(@NotNull ClientHttpResponse response) throws IOException {

                var statusCode = response.getStatusCode();

                byte[] bodyBytes = response.getBody().readAllBytes();
                ApiErrorResponse errorResponse;
                if (bodyBytes.length > 0) {
                    errorResponse = objectMapper.readValue(
                        new String(response.getBody().readAllBytes(), DEFAULT_BODY_ENCODING),
                        ApiErrorResponse.class
                    );
                } else {
                    errorResponse = new ApiErrorResponse(
                        "Empty response", null, null, null, null
                    );
                }

                if (statusCode.is1xxInformational()) {

                    throw new UnexpectedResponse(
                        response.getStatusCode().value(),
                        errorResponse.getExceptionMessage()
                    );

                } else if (statusCode.is3xxRedirection()) {

                    throw new UnexpectedResponse(
                        response.getStatusCode().value(),
                        errorResponse.getExceptionMessage()
                    );

                } else if (statusCode.is4xxClientError()) {

                    if (response.getStatusCode() == HttpStatus.BAD_REQUEST) {
                        throw new IncorrectRequestException(errorResponse.getExceptionMessage());
                    } else {
                        throw new UnexpectedResponse(
                            response.getStatusCode().value(),
                            errorResponse.getExceptionMessage()
                        );
                    }

                } else if (statusCode.is5xxServerError()) {

                    throw new UnexpectedResponse(
                        response.getStatusCode().value(),
                        errorResponse.getExceptionMessage()
                    );

                }

            }
        }
}
