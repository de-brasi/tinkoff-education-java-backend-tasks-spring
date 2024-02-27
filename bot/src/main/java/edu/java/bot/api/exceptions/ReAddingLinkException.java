package edu.java.bot.api.exceptions;

public class ReAddingLinkException extends Exception {
    @SuppressWarnings("AvoidNoArgumentSuperConstructorCall")
    public ReAddingLinkException() {
        super();
    }

    public ReAddingLinkException(String errorMessage) {
        super(errorMessage);
    }
}