package edu.java.bot.core.commands;

@FunctionalInterface
public interface CommandArgumentValidator {
    public boolean validate(String argToValidate);
}
