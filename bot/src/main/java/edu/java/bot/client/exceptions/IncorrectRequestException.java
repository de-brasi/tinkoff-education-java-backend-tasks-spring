package edu.java.bot.client.exceptions;

public class IncorrectRequestException extends RuntimeException {
    @SuppressWarnings("AvoidNoArgumentSuperConstructorCall")
    public IncorrectRequestException() {
        super();
    }

    public IncorrectRequestException(String errorMessage) {
        super(errorMessage);
    }
}
