package edu.java.data.domain.exceptions;

public class InvalidArgumentForTypeInDataBase extends RuntimeException {
    public InvalidArgumentForTypeInDataBase(Exception e) {
        super(e);
    }

    public InvalidArgumentForTypeInDataBase(String errorMessage) {
        super(errorMessage);
    }
}
