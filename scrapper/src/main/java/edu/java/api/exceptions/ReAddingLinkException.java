package edu.java.api.exceptions;

public class ReAddingLinkException extends RuntimeException {
    @SuppressWarnings("AvoidNoArgumentSuperConstructorCall")
    public ReAddingLinkException() {
        super();
    }

    public ReAddingLinkException(String errorMessage) {
        super(errorMessage);
    }
}
