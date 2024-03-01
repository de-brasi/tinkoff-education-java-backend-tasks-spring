package edu.java.bot.api;

import java.util.Arrays;
import edu.common.dtos.ApiErrorResponse;
import edu.common.exceptions.ChatIdNotExistsException;
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
