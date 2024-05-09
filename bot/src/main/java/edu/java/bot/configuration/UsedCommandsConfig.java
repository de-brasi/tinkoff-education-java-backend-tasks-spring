package edu.java.bot.configuration;

import edu.java.bot.client.ScrapperClient;
import edu.java.bot.core.commands.CommandArgumentDescription;
import edu.java.bot.core.commands.TelegramBotCommand;
import edu.java.bot.core.util.Link;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
@SuppressWarnings("LambdaBodyLength")
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

                final String greetingMessage =
                    "You have been successfully registered. To find out the available commands use the `/help` command";

                usedBot.sendPlainTextMessage(context.getChatId(), greetingMessage);
                log.info("Command 'start' was called by user with telegram chat id: " + context.getChatId());

                try {
                    scrapperClient.registerChat(context.getChatId());
                } catch (Exception e) {
                    logException(e, commandName);
                }

            });
    }

    @Bean("commandHelp")
    @SuppressWarnings("LineLength")
    TelegramBotCommand commandHelp() {
        final String commandName = "help";
        return new TelegramBotCommand()
            .withCommandName(commandName)
            .withCommandTextDescription("get list of commands")
            .withCallAction((usedBot, context) -> {

                usedBot.sendMarkdownV2Message(context.getChatId(), """
                    All available commands:
                    • *start* \\- used for registration \\(called automatically when starting using bot\\);
                    • *help* \\- explain all commands, already called;
                    • *track* \\(interested links\\) \\- use this command to ask bot notify you if content of links in argument was changed
                    • *untrack* \\(already not interested links\\) \\- deny to notify if content was changed
                    • *list* \\- show all interested links
                    """);

                log.info("Command 'help' was called by user with telegram chat id: " + context.getChatId());

            });
    }

    @Bean("commandTrack")
    @SuppressWarnings("MultipleStringLiterals")
    TelegramBotCommand commandTrack() {
        final String commandName = "track";
        return new TelegramBotCommand()
            .withCommandName(commandName)
            .withCommandTextDescription("track links in arguments")
            .withOrderedArguments(CommandArgumentDescription.of(Link::validate, true))
            .withCallAction((usedBot, context) -> {

                var links = context.getCommand().args();
                var chatId = context.getChatId();
                var actuallyTrackedLinks = new ArrayList<String>();

                // todo: bottle-neck caused by blocking retry mechanism
                for (var link: links) {
                    try {
                        var response = scrapperClient.trackLink(chatId, link);
                        actuallyTrackedLinks.add(link);
                    } catch (Exception e) {
                        logException(e, commandName);
                    }
                }

                String responseMessage;

                if (actuallyTrackedLinks.isEmpty()) {
                    responseMessage = "Please write the links you are interested in";
                    usedBot.sendPlainTextMessage(chatId, responseMessage);
                    log.info("""
                        Command 'track' was called by user with telegram chat id: {};
                        No links got in arguments;
                        """,
                        chatId);
                } else if (actuallyTrackedLinks.size() == 1) {
                    responseMessage = "Now you tracking link with URL %s".formatted(links.get(0));
                    usedBot.sendPlainTextMessage(chatId, responseMessage);
                    log.info("""
                        Command 'track' was called by user with telegram chat id: {};
                        Got link: {}
                        """,
                        chatId,
                        actuallyTrackedLinks.get(0));
                } else {
                    responseMessage = "Now you tracking link with URL %s".formatted(actuallyTrackedLinks.get(0));
                    usedBot.sendPlainTextMessage(chatId, responseMessage);
                    final String allTrackedLinks = String.join("\n-- ", actuallyTrackedLinks);
                    log.info("""
                        Command 'track' was called by user with telegram chat id: {};
                        Got links:
                        -- {}
                        """,
                        chatId,
                        allTrackedLinks);
                }

            });
    }

    @Bean("commandUntrack")
    @SuppressWarnings("MultipleStringLiterals")
    TelegramBotCommand commandUntrack() {
        final String commandName = "untrack";
        return new TelegramBotCommand()
            .withCommandName(commandName)
            .withCommandTextDescription("untrack links in arguments")
            .withOrderedArguments(
                CommandArgumentDescription.of(Link::validate, true)
            )
            .withCallAction((usedBot, context) -> {

                var links = context.getCommand().args();
                var chatId = context.getChatId();
                var actuallyUntrackedLinks = new ArrayList<String>();

                // todo: bottle-neck caused by blocking retry mechanism
                for (var link: links) {
                    try {
                        var response = scrapperClient.untrackLink(chatId, link);
                        actuallyUntrackedLinks.add(link);
                    } catch (Exception e) {
                        logException(e, commandName);
                    }
                }

                String responseMessage;

                if (actuallyUntrackedLinks.isEmpty()) {
                    responseMessage = "Please write the links you are want to untrack";
                    usedBot.sendPlainTextMessage(chatId, responseMessage);
                    log.info("""
                        Command 'untrack' was called by user with telegram chat id: {};
                        No links got in arguments;
                        """,
                        chatId);
                } else if (actuallyUntrackedLinks.size() == 1) {
                    responseMessage = "Now you NOT tracking link with URL %s".formatted(links.get(0));
                    usedBot.sendPlainTextMessage(chatId, responseMessage);
                    log.info("""
                        Command 'untrack' was called by user with telegram chat id: {};
                        Got link: {}
                        """,
                        chatId,
                        actuallyUntrackedLinks.get(0));
                } else {
                    responseMessage = "Now you NOT tracking link with URL %s".formatted(actuallyUntrackedLinks.get(0));
                    usedBot.sendPlainTextMessage(chatId, responseMessage);
                    final String allTrackedLinks = String.join("\n-- ", actuallyUntrackedLinks);
                    log.info("""
                        Command 'untrack' was called by user with telegram chat id: {};
                        Got links:
                        -- {}
                        """,
                        chatId,
                        allTrackedLinks);
                }

            });
    }

    @Bean("commandList")
    TelegramBotCommand commandList() {
        final String commandName = "list";
        return new TelegramBotCommand()
            .withCommandName(commandName)
            .withCommandTextDescription("show tracked links")
            .withCallAction((usedBot, context) -> {

                log.info("Command 'list' was called by user with telegram chat id: " + context.getChatId());
                var chatId = context.getChatId();

                try {
                    var response = scrapperClient.getAllTrackedLinks(context.getChatId());
                    List<String> trackedLinks = response.getLinks()
                        .stream()
                        .map(item -> {
                            try {
                                return item.getUrl().toURL().toString();
                            } catch (MalformedURLException e) {
                                throw new RuntimeException(e);
                            }
                        })
                        .toList();
                    final String responseMessage = "You track following links:\n• "
                        + String.join("\n• ", trackedLinks);
                    usedBot.sendPlainTextMessage(chatId, responseMessage);
                } catch (Exception e) {
                    logException(e, commandName);
                }

            });
    }

    @Bean("unexpectedCommand")
    TelegramBotCommand unexpectedCommand() {
        return new TelegramBotCommand()
            .withCallAction((usedBot, context) -> {
                usedBot.sendPlainTextMessage(
                    context.getChatId(),
                    """
                        Unexpected command or argument.
                        Please, check out your query!
                        You can see all supported commands with command `/help`
                        """
                );
                log.info(
                    "Unexpected command '{}' was called by user with telegram chat id: {}",
                    context.getCommand().name(),
                    context.getChatId()
                );
            });
    }

    private static void logException(Exception e, String commandName) {
        log.error(("""
            Exception occurs when command %s action!
            Exception name: %s
            Exception message: %s
            Stacktrace:
            %s
            """)
            .formatted(
                commandName,
                e.getClass().getCanonicalName(),
                e.getMessage(),
                Arrays.stream(e.getStackTrace())
                    .map(StackTraceElement::toString)
                    .collect(Collectors.joining("\n"))
            )
        );
    }
}
