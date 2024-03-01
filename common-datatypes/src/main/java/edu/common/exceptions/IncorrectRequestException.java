package edu.common.exceptions;

public class IncorrectRequestException extends RuntimeException {
    @SuppressWarnings("AvoidNoArgumentSuperConstructorCall")
    public IncorrectRequestException() {
        super();
    }

    public IncorrectRequestException(String errorMessage) {
        super(errorMessage);
    }
}
