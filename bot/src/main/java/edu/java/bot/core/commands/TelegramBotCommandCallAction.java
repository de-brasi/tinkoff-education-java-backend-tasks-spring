package edu.java.bot.core.commands;

import com.pengrad.telegrambot.TelegramBot;

@FunctionalInterface
public interface TelegramBotCommandCallAction {
    public void call(TelegramBot targetBot);
}
