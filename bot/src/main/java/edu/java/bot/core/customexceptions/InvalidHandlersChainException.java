package edu.java.bot.core.customexceptions;

public class InvalidHandlersChainException extends RuntimeException {
    public InvalidHandlersChainException(String errorMessage) {
        super(errorMessage);
    }
}
