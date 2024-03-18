package edu.java.domain.exceptions;

// todo: это не во что не мапить - отловится RestControllerAdvice'ом
public class DataBaseInteractingException extends RuntimeException {
    public DataBaseInteractingException(String errorMessage) {
        super(errorMessage);
    }

    public DataBaseInteractingException() {}

    public DataBaseInteractingException(Exception exception) {
        super(exception);
    }
}
