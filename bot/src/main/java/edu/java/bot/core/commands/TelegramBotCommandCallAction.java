package edu.java.bot.core.commands;

import com.pengrad.telegrambot.TelegramBot;
import edu.java.bot.entities.CommandCallContext;

@FunctionalInterface
public interface TelegramBotCommandCallAction {
    public void call(TelegramBot targetBot, CommandCallContext context);
}
