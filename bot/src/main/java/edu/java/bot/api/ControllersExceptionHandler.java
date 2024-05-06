package edu.java.bot.api;

import edu.common.datatypes.dtos.ApiErrorResponse;
import edu.common.datatypes.exceptions.ChatIdNotExistsException;
import java.util.Arrays;
import org.springframework.http.HttpStatus;
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
            HttpStatus.BAD_REQUEST
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
            HttpStatus.BAD_REQUEST
        );
    }
}
