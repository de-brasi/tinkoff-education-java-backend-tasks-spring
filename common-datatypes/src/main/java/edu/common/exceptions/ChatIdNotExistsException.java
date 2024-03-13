package edu.common.exceptions;

public class ChatIdNotExistsException extends RuntimeException {
    @SuppressWarnings("AvoidNoArgumentSuperConstructorCall")
    public ChatIdNotExistsException() {
        super();
    }

    public ChatIdNotExistsException(String errorMessage) {
        super(errorMessage);
    }
}
