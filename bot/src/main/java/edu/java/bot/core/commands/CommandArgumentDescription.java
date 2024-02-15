package edu.java.bot.core.commands;

public record CommandArgumentDescription(CommandArgumentValidator validator, boolean isTrailingAndNotOne) {}
