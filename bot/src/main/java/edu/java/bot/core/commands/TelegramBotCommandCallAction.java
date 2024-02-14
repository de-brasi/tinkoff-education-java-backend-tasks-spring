package edu.java.bot.core.commands;

import edu.java.bot.core.TelegramBotWrapper;

@FunctionalInterface
public interface TelegramBotCommandCallAction {
    public void call(TelegramBotWrapper targetBot);
}
