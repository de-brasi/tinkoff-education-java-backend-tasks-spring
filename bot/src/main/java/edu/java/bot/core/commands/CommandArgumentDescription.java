package edu.java.bot.core.commands;

public record CommandArgumentDescription(CommandArgumentValidator validator, boolean isTrailingAndNotOne) {
    public static CommandArgumentDescription of(CommandArgumentValidator validator, boolean isTrailingAndNotOne) {
        return new CommandArgumentDescription(validator, isTrailingAndNotOne);
    }
}
