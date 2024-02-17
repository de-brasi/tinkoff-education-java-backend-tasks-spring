package edu.java.bot.core.commands;

import edu.java.bot.core.util.Link;
import edu.java.bot.repository.interfaces.UsersRepository;

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

    public static TelegramBotCommand track(UsersRepository repository) {
        return new TelegramBotCommand()
            .withCommandName("track")
            .withCommandTextDescription("track links in arguments")
            .withOrderedArguments(
                CommandArgumentDescription.of(Link::validate, true)
            )
            .withCallAction((usedBot, context) -> {
                var userId = context.getUser().getTelegramId();
                var links = context.getCommand().args();
                repository.storeLinksForUser(userId, links);
                usedBot.sendPlainTextMessage(
                    context.getChatId(),
                    "Tracked links:\n" + String.join("\n", links)
                );
            });
    }

    public static TelegramBotCommand untrack(UsersRepository repository) {
        return new TelegramBotCommand()
            .withCommandName("untrack")
            .withCommandTextDescription("untrack links in arguments")
            .withOrderedArguments(
                CommandArgumentDescription.of(Link::validate, true)
            )
            .withCallAction((usedBot, context) -> {
                var userId = context.getUser().getTelegramId();
                var links = context.getCommand().args();
                repository.deleteLinksForUser(userId, links);
                usedBot.sendPlainTextMessage(
                    context.getChatId(),
                    "Untracked links:\n" + String.join("\n", links)
                );
            });
    }

    public static TelegramBotCommand list(UsersRepository repository) {
        return new TelegramBotCommand()
            .withCommandName("list")
            .withCommandTextDescription("show tracked links")
            .withCallAction((usedBot, context) -> {
                var userId = context.getUser().getTelegramId();
                var links = repository.getLinksForUser(userId);
                usedBot.sendPlainTextMessage(
                    context.getChatId(),
                    "Tracked links is:\n" + String.join("\n", links)
                );
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
