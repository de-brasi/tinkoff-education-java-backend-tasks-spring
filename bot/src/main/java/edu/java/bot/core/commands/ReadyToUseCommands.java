package edu.java.bot.core.commands;

import edu.java.bot.core.util.Link;

public class ReadyToUseCommands {
    private ReadyToUseCommands() {}

    public static TelegramBotCommand help() {
        return new TelegramBotCommand()
            .withCommandName("help")
            .withCommandTextDescription(
                "тут будет описание команд, а заодно проверка русского текста")
            .withCallAction((usedBot, context) -> {
                usedBot.sendPlainTextMessage(context.getChatId(), "command 'help' was called");
            });
    }

    public static TelegramBotCommand track() {
        return new TelegramBotCommand()
            .withCommandName("track")
            .withCommandTextDescription("track command used")
            .withOrderedArguments(
                CommandArgumentDescription.of(Link::validate, true)
            )
            .withCallAction((usedBot, context) -> {
                usedBot.sendPlainTextMessage(context.getChatId(), "track links validated");
            });
    }

    public static TelegramBotCommand untrack() {
        return new TelegramBotCommand()
            .withCommandName("untrack")
            .withCommandTextDescription("untrack command used")
            .withOrderedArguments(
                CommandArgumentDescription.of(Link::validate, true)
            )
            .withCallAction((usedBot, context) -> {
                usedBot.sendPlainTextMessage(context.getChatId(), "untrack links validated");
            });
    }

    public static TelegramBotCommand list() {
        return new TelegramBotCommand()
            .withCommandName("list")
            .withCommandTextDescription("use command list")
            .withCallAction((usedBot, context) -> {
                usedBot.sendPlainTextMessage(context.getChatId(), "command 'list' was called");
            });
    }

    public static TelegramBotCommand unexpectedCommand() {
        return new TelegramBotCommand()
            .withCallAction(
                (usedBot, context) -> usedBot.sendPlainTextMessage(
                    context.getChatId(),
                    "Unexpected command or argument. Please, check out your query!"
                )
            );
    }
}
