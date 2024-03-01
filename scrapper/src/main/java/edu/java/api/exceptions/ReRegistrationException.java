package edu.java.api.exceptions;

public class ReRegistrationException extends RuntimeException {
    @SuppressWarnings("AvoidNoArgumentSuperConstructorCall")
    public ReRegistrationException() {
        super();
    }

    public ReRegistrationException(String errorMessage) {
        super(errorMessage);
    }
}
