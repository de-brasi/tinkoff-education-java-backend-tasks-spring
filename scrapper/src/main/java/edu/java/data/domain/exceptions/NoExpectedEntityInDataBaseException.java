package edu.java.data.domain.exceptions;

public class NoExpectedEntityInDataBaseException extends RuntimeException {
    public NoExpectedEntityInDataBaseException(String entityTypeName) {
        super("No entity '%s' with expected description found in database!".formatted(entityTypeName));
    }
}
