package edu.java.bot.core.commands;

import edu.java.bot.core.entities.CommandCallContext;
import edu.java.bot.services.TelegramBotWrapper;

@FunctionalInterface
public interface TelegramBotCommandCallAction {
    void call(TelegramBotWrapper targetBot, CommandCallContext context);
}
