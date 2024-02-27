package edu.java.api.exceptions;

public class ChatIdNotExistsException extends Exception {
    @SuppressWarnings("AvoidNoArgumentSuperConstructorCall")
    public ChatIdNotExistsException() {
        super();
    }

    public ChatIdNotExistsException(String errorMessage) {
        super(errorMessage);
    }
}
