package edu.java.api;

import edu.common.datatypes.dtos.ApiErrorResponse;
import edu.common.datatypes.exceptions.ChatIdNotExistsException;
import edu.common.datatypes.exceptions.LinksNotAddedException;
import edu.common.datatypes.exceptions.ReAddingLinkException;
import edu.common.datatypes.exceptions.ReRegistrationException;
import java.util.Arrays;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
@Slf4j
@SuppressWarnings("MultipleStringLiterals")
public class ControllersExceptionHandler {

    @ExceptionHandler(ChatIdNotExistsException.class)
    public ResponseEntity<ApiErrorResponse> invalidChatExceptionHandler(Exception e) {
        return new ResponseEntity<>(
            new ApiErrorResponse(
                "chat id not exists stub error",
                "1",
                e.getClass().getCanonicalName(),
                e.getMessage(),
                Arrays.stream(e.getStackTrace())
                    .map(StackTraceElement::toString)
                    .toList()
            ),
            HttpStatus.NOT_FOUND
        );
    }

    @ExceptionHandler(LinksNotAddedException.class)
    public ResponseEntity<ApiErrorResponse> linkNotFoundErrorHandler(Exception e) {
        return new ResponseEntity<>(
            new ApiErrorResponse(
                "no such link stored stub error",
                "2",
                e.getClass().getCanonicalName(),
                e.getMessage(),
                Arrays.stream(e.getStackTrace())
                    .map(StackTraceElement::toString)
                    .toList()
            ),
            HttpStatus.NOT_FOUND
        );
    }

    @ExceptionHandler(ReAddingLinkException.class)
    public ResponseEntity<ApiErrorResponse> doubleAddedLinkExceptionHandler(Exception e) {
        return new ResponseEntity<>(
            new ApiErrorResponse(
                "double adding link stub error",
                "000",
                e.getClass().getCanonicalName(),
                e.getMessage(),
                Arrays.stream(e.getStackTrace())
                    .map(StackTraceElement::toString)
                    .toList()
            ),
            HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(ReRegistrationException.class)
    public ResponseEntity<ApiErrorResponse> doubleRegistrationExceptionHandler(Exception e) {
        return new ResponseEntity<>(
            new ApiErrorResponse(
                "double registration stub error",
                "3",
                e.getClass().getCanonicalName(),
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
                "4",
                e.getClass().getCanonicalName(),
                e.getMessage(),
                Arrays.stream(e.getStackTrace())
                    .map(StackTraceElement::toString)
                    .toList()
            ),
            HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler({
        MissingRequestHeaderException.class,
        MethodArgumentTypeMismatchException.class
    })
    public ResponseEntity<ApiErrorResponse> incompleteRequestHandler(Exception e) {
        return new ResponseEntity<>(
            new ApiErrorResponse(
                "internal stub error",
                "5",
                e.getClass().getCanonicalName(),
                e.getMessage(),
                Arrays.stream(e.getStackTrace())
                    .map(StackTraceElement::toString)
                    .toList()
            ),
            HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> internalErrorHandler(Exception e) {
        log.info(
            "Exception caught in ExceptionHandler with name: " + e.getClass().getCanonicalName()
                + "\nstack trace is:\n"
                + Arrays.stream(e.getStackTrace())
                .map(StackTraceElement::toString)
                .collect(Collectors.joining("\n"))
        );

        return new ResponseEntity<>(
            new ApiErrorResponse(
                "internal stub error",
                "6",
                e.getClass().getCanonicalName(),
                e.getMessage(),
                Arrays.stream(e.getStackTrace())
                    .map(StackTraceElement::toString)
                    .toList()
            ),
            HttpStatus.INTERNAL_SERVER_ERROR
        );
    }
}
