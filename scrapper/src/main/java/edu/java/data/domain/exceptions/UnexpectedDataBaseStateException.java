package edu.java.data.domain.exceptions;

public class UnexpectedDataBaseStateException extends RuntimeException {
    public UnexpectedDataBaseStateException(Exception e) {
        super(e);
    }

    public UnexpectedDataBaseStateException(String message) {
        super(message);
    }
}
