package edu.java.bot.api.exceptions;

public class ChatIdNotExistsException extends Exception {
    public ChatIdNotExistsException() {
        super();
    }

    public ChatIdNotExistsException(String errorMessage) {
        super(errorMessage);
    }
}
