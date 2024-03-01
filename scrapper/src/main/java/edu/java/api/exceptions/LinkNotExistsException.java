package edu.java.api.exceptions;

public class LinkNotExistsException extends RuntimeException {
    @SuppressWarnings("AvoidNoArgumentSuperConstructorCall")
    public LinkNotExistsException() {
        super();
    }

    public LinkNotExistsException(String errorMessage) {
        super(errorMessage);
    }
}
