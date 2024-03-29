package edu.common.datatypes.exceptions.httpresponse;

import edu.common.datatypes.dtos.ApiErrorResponse;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;

@Getter
public class BadHttpResponseException extends RuntimeException {
    public BadHttpResponseException(@NotNull HttpStatus httpCode, @NotNull ApiErrorResponse errorResponse) {
        this.httpCode = httpCode;
        this.errorResponse = errorResponse;
    }

    private final HttpStatus httpCode;
    private final ApiErrorResponse errorResponse;
}
