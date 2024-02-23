package edu.java.bot.customexceptions;

public class NullTelegramTokenException extends Exception {
    public NullTelegramTokenException(String errorMessage) {
        super(errorMessage);
    }
}
