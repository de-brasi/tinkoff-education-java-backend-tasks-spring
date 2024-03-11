package edu.java.bot.customexceptions;

public class InvalidHandlersChainException extends RuntimeException {
    public InvalidHandlersChainException(String errorMessage) {
        super(errorMessage);
    }
}
