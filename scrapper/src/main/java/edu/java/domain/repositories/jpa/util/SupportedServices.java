package edu.java.domain.repositories.jpa.util;

public enum SupportedServices {
    GITHUB,
    STACKOVERFLOW;

    public String databaseRepresentation() {
        return switch (this) {
            case GITHUB -> "github";
            case STACKOVERFLOW -> "stackoverflow";
        };
    }
}
