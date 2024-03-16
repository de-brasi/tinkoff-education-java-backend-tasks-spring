package edu.common.exceptions;

public class LinksNotAddedException extends RuntimeException {
    @SuppressWarnings("AvoidNoArgumentSuperConstructorCall")
    public LinksNotAddedException() {
        super();
    }

    public LinksNotAddedException(String errorMessage) {
        super(errorMessage);
    }
}
