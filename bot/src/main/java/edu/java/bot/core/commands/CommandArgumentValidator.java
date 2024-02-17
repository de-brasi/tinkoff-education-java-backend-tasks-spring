package edu.java.bot.core.commands;

@FunctionalInterface
public interface CommandArgumentValidator {
    boolean validate(String argToValidate);
}
