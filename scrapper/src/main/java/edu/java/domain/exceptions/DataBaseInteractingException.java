package edu.java.domain.exceptions;

public class DataBaseInteractingException extends RuntimeException {
    public DataBaseInteractingException(String errorMessage) {
        super(errorMessage);
    }

    public DataBaseInteractingException() {}

    public DataBaseInteractingException(Exception exception) {
        super(exception);
    }
}
