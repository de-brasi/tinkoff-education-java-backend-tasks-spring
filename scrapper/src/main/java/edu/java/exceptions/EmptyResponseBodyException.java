package edu.java.exceptions;

public class EmptyResponseBodyException extends Exception {
    public EmptyResponseBodyException(String errorMessage) {
        super(errorMessage);
    }
}
