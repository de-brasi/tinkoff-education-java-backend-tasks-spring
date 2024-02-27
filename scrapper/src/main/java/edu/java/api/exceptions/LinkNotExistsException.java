package edu.java.api.exceptions;

public class LinkNotExistsException extends Exception {
    @SuppressWarnings("AvoidNoArgumentSuperConstructorCall")
    public LinkNotExistsException() {
        super();
    }

    public LinkNotExistsException(String errorMessage) {
        super(errorMessage);
    }
}
