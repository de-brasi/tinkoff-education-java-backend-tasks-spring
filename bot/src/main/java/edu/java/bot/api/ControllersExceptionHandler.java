package edu.java.bot.api;

import edu.java.bot.api.dtos.ApiErrorResponse;
import edu.java.bot.api.exceptions.ChatIdNotExistsException;
import edu.java.bot.api.exceptions.ReAddingLinkException;
import edu.java.bot.api.exceptions.ReRegistrationException;
import java.util.Arrays;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@SuppressWarnings("MultipleStringLiterals")
public class ControllersExceptionHandler {
    @ExceptionHandler(ChatIdNotExistsException.class)
    public ResponseEntity<ApiErrorResponse> invalidChatExceptionHandler(Exception e) {
        return new ResponseEntity<>(
            new ApiErrorResponse(
                "chat id not exists stub error",
                "000",
                "ChatIdNotExistsException",
                e.getMessage(),
                Arrays.stream(e.getStackTrace())
                    .map(StackTraceElement::toString)
                    .toList()
            ),
            HttpStatusCode.valueOf(HttpStatus.BAD_REQUEST.value())
        );
    }

    @ExceptionHandler(ReAddingLinkException.class)
    public ResponseEntity<ApiErrorResponse> doubleAddedLinkExceptionHandler(Exception e) {
        return new ResponseEntity<>(
            new ApiErrorResponse(
                "double adding link stub error",
                "000",
                "ReAddingLinkException",
                e.getMessage(),
                Arrays.stream(e.getStackTrace())
                    .map(StackTraceElement::toString)
                    .toList()
            ),
            HttpStatusCode.valueOf(HttpStatus.BAD_REQUEST.value())
        );
    }

    @ExceptionHandler(ReRegistrationException.class)
    public ResponseEntity<ApiErrorResponse> doubleRegistrationExceptionHandler(Exception e) {
        return new ResponseEntity<>(
            new ApiErrorResponse(
                "double registration stub error",
                "000",
                "ReRegistrationException",
                e.getMessage(),
                Arrays.stream(e.getStackTrace())
                    .map(StackTraceElement::toString)
                    .toList()
            ),
            HttpStatusCode.valueOf(HttpStatus.BAD_REQUEST.value())
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> notReadableExceptionHandler(Exception e) {
        return new ResponseEntity<>(
            new ApiErrorResponse(
                "not readable exception stub error",
                "000",
                "HttpMessageNotReadableException",
                e.getMessage(),
                Arrays.stream(e.getStackTrace())
                    .map(StackTraceElement::toString)
                    .toList()
            ),
            HttpStatusCode.valueOf(HttpStatus.BAD_REQUEST.value())
        );
    }
}
