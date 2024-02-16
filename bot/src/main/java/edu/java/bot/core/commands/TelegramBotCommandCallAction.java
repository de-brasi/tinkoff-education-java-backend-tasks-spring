package edu.java.bot.core.commands;

import edu.java.bot.services.TelegramBotWrapper;
import edu.java.bot.entities.CommandCallContext;

@FunctionalInterface
public interface TelegramBotCommandCallAction {
    public void call(TelegramBotWrapper targetBot, CommandCallContext context);
}
