package edu.java.bot.api.exceptions;

public class ReRegistrationException extends Exception {
    public ReRegistrationException() {
        super();
    }

    public ReRegistrationException(String errorMessage) {
        super(errorMessage);
    }
}
