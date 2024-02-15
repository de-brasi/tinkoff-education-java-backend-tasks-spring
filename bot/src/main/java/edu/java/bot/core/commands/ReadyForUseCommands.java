package edu.java.bot.core.commands;

public class ReadyForUseCommands {
    private ReadyForUseCommands() {}

    public static TelegramBotCommand start() {
        return new TelegramBotCommand()
            .withCommandName("a")
            .withCommandTextDescription("use command a")
            .withCallAction((usedBot, context) -> {
                usedBot.sendPlainTextMessage(context.getChatId(), "command 'a' was called");
            });
    }

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
            .withCallAction((usedBot, context) -> {
                usedBot.sendPlainTextMessage(context.getChatId(), "command 'track' was called");
            });
    }

    public static TelegramBotCommand untrack() {
        return new TelegramBotCommand()
            .withCommandName("untrack")
            .withCommandTextDescription("untrack command used")
            .withCallAction((usedBot, context) -> {
                usedBot.sendPlainTextMessage(context.getChatId(), "command 'untrack' was called");
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
