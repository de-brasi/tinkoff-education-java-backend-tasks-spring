package edu.java.bot.client.exceptions;

public class UnexpectedResponse extends RuntimeException {
    public UnexpectedResponse(int responseCode, String errorMessage) {
        super(
            "Unexpected exception for response with code %d and message '%s'"
                .formatted(responseCode, errorMessage)
        );
    }
}
