package edu.java.bot.configuration;

import edu.java.bot.client.ScrapperClient;
import edu.java.bot.core.commands.CommandArgumentDescription;
import edu.java.bot.core.commands.TelegramBotCommand;
import edu.java.bot.core.util.Link;
import java.util.Arrays;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UsedCommandsConfig {
    public UsedCommandsConfig(@Autowired ScrapperClient scrapperClient) {
        this.scrapperClient = scrapperClient;
    }

    private final ScrapperClient scrapperClient;

    @Bean("commandStart")
    TelegramBotCommand commandStart() {
        final String commandName = "start";
        return new TelegramBotCommand()
            .withCommandName(commandName)
            .withCommandTextDescription("register me")
            .withCallAction((usedBot, context) -> {
                    // todo:
                    //  - исправить сообщение пользователю

                    usedBot.sendPlainTextMessage(context.getChatId(), "command 'start' was called");
                    LOGGER.info("...Command start called...");

                    try {
                        scrapperClient.registerChat(context.getChatId());
                    } catch (Exception e) {
                        logException(e, commandName);
                    }
                }
            );
    }

    @Bean("commandHelp")
    TelegramBotCommand commandHelp() {
        final String commandName = "help";
        return new TelegramBotCommand()
            .withCommandName(commandName)
            .withCommandTextDescription("get list of commands")
            .withCallAction((usedBot, context) -> {
                    // todo: исправить сообщение пользователю
                    usedBot.sendPlainTextMessage(
                        context.getChatId(),
                        "Тут будет большое и подробное описание команд"
                    );
                    LOGGER.info("...Command help called...");
                }
            );
    }

    @Bean("commandTrack")
    TelegramBotCommand commandTrack() {
        final String commandName = "track";
        return new TelegramBotCommand()
            .withCommandName(commandName)
            .withCommandTextDescription("track links in arguments")
            .withOrderedArguments(CommandArgumentDescription.of(Link::validate, true))
            .withCallAction((usedBot, context) -> {
                    // todo:
                    //  - исправить сообщение пользователю

                    var links = context.getCommand().args();
                    usedBot.sendPlainTextMessage(
                        context.getChatId(),
                        "Tracked links:\n" + String.join("\n", links)
                    );
                    LOGGER.info("...Command track called...");

                    try {
                        // todo:
                        //  Пока отправлять только одну ссылку из пачки,
                        //  чтобы не париться с обработкой ошибок
                        //  и формированием соответствующего ответа.
                        scrapperClient.trackLink(context.getChatId(), links.get(0));
                    } catch (Exception e) {
                        logException(e, commandName);
                    }
                }
            );
    }

    @Bean("commandUntrack")
    TelegramBotCommand commandUntrack() {
        final String commandName = "untrack";
        return new TelegramBotCommand()
            .withCommandName(commandName)
            .withCommandTextDescription("untrack links in arguments")
            .withOrderedArguments(
                CommandArgumentDescription.of(Link::validate, true)
            )
            .withCallAction((usedBot, context) -> {
                    // todo:
                    //  - исправить сообщение пользователю

                    var links = context.getCommand().args();
                    usedBot.sendPlainTextMessage(
                        context.getChatId(),
                        "Untracked links:\n" + String.join("\n", links)
                    );
                    LOGGER.info("...Command untrack called...");

                    try {
                        // todo:
                        //  Пока отправлять только одну ссылку из пачки,
                        //  чтобы не париться с обработкой ошибок
                        //  и формированием соответствующего ответа.
                        scrapperClient.untrackLink(context.getChatId(), links.get(0));
                    } catch (Exception e) {
                        logException(e, commandName);
                    }
                }
            );
    }

    @Bean("commandList")
    TelegramBotCommand commandList() {
        final String commandName = "list";
        return new TelegramBotCommand()
            .withCommandName(commandName)
            .withCommandTextDescription("show tracked links")
            .withCallAction((usedBot, context) -> {
                    // todo:
                    //  - исправить сообщение пользователю
                    LOGGER.info("...Command list called...");

                    try {
                        var resp = scrapperClient.getAllTrackedLinks(context.getChatId());
                        usedBot.sendPlainTextMessage(
                            context.getChatId(),
                            "Response: " + resp.toString()
                        );
                    } catch (Exception e) {
                        logException(e, commandName);
                    }
                }
            );
    }

    @Bean("unexpectedCommand")
    TelegramBotCommand unexpectedCommand() {
        return new TelegramBotCommand()
            .withCallAction((usedBot, context) -> {
                    usedBot.sendPlainTextMessage(
                        context.getChatId(),
                        "Unexpected command or argument. Please, check out your query!"
                    );
                    LOGGER.info("...Unexpected command called...");
                }
            );
    }

    private static void logException(Exception e, String commandName) {
        LOGGER.info((
                """
                    Exception occurs when command %s action!
                    Exception name: %s
                    Exception message: %s
                    Stacktrace:
                    %s
                    """
            ).formatted(
                commandName,
                e.getClass().getCanonicalName(),
                e.getMessage(),
                Arrays.stream(e.getStackTrace())
                    .map(StackTraceElement::toString)
                    .collect(Collectors.joining("\n"))
            )
        );
    }

    private final static Logger LOGGER = LogManager.getLogger();
}
