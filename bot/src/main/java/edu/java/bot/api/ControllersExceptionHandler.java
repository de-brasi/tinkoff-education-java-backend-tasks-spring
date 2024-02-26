package edu.java.bot.api;

import edu.java.bot.api.dtos.ApiErrorResponse;
import edu.java.bot.api.exceptions.ChatIdNotExistsException;
import edu.java.bot.api.exceptions.ReAddingLinkException;
import edu.java.bot.api.exceptions.ReRegistrationException;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.Arrays;

@RestControllerAdvice
public class ControllersExceptionHandler {
    @ExceptionHandler(ChatIdNotExistsException.class)
    public ResponseEntity<ApiErrorResponse> invalidChatExceptionHandler(Exception e) {
        return new ResponseEntity<>(
            new ApiErrorResponse(
                "chat id not exists stub error",
                "000",
                "ChatIdNotExistsException",
                "stub",
                Arrays.stream(e.getStackTrace())
                    .map(StackTraceElement::toString)
                    .toList()
            ),
            HttpStatusCode.valueOf(400)
        );
    }

    @ExceptionHandler(ReAddingLinkException.class)
    public ResponseEntity<ApiErrorResponse> doubleAddedLinkExceptionHandler(Exception e) {
        return new ResponseEntity<>(
            new ApiErrorResponse(
                "double adding link stub error",
                "000",
                "ReAddingLinkException",
                "stub",
                Arrays.stream(e.getStackTrace())
                    .map(StackTraceElement::toString)
                    .toList()
            ),
            HttpStatusCode.valueOf(400)
        );
    }

    @ExceptionHandler(ReRegistrationException.class)
    public ResponseEntity<ApiErrorResponse> doubleRegistrationExceptionHandler(Exception e) {
        return new ResponseEntity<>(
            new ApiErrorResponse(
                "double registration stub error",
                "000",
                "ReRegistrationException",
                "stub",
                Arrays.stream(e.getStackTrace())
                    .map(StackTraceElement::toString)
                    .toList()
            ),
            HttpStatusCode.valueOf(400)
        );
    }
}
