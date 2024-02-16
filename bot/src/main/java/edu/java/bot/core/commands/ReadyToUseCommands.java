package edu.java.bot.core.commands;

import edu.java.bot.core.util.Link;

public class ReadyToUseCommands {
    private ReadyToUseCommands() {}

    public static TelegramBotCommand start() {
        return new TelegramBotCommand()
            .withCommandName("start")
            .withCommandTextDescription("register me")
            .withCallAction((usedBot, context) -> {
                usedBot.sendPlainTextMessage(context.getChatId(), "command 'start' was called");
            });
    }

    public static TelegramBotCommand help() {
        return new TelegramBotCommand()
            .withCommandName("help")
            .withCommandTextDescription(
                "get list of commands")
            .withCallAction((usedBot, context) -> {
                usedBot.sendPlainTextMessage(context.getChatId(), "command 'help' was called");
            });
    }

    public static TelegramBotCommand track() {
        return new TelegramBotCommand()
            .withCommandName("track")
            .withCommandTextDescription("track links in arguments")
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
            .withCommandTextDescription("untrack links in arguments")
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
            .withCommandTextDescription("show tracked links")
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
