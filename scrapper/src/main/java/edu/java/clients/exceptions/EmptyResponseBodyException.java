package edu.java.clients.exceptions;

public class EmptyResponseBodyException extends Exception {
    public EmptyResponseBodyException(String errorMessage) {
        super(errorMessage);
    }
}
