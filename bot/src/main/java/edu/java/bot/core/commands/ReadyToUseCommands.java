package edu.java.bot.core.commands;

import edu.java.bot.core.util.Link;
import edu.java.bot.repository.interfaces.UsersRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ReadyToUseCommands {
    // TODO:
    //  сделать вставку (injection) ScrapperClient,
    //  добавить команду для отмены регистрации чата

    private ReadyToUseCommands() {
    }

    public static TelegramBotCommand start() {
        return new TelegramBotCommand()
            .withCommandName("start")
            .withCommandTextDescription("register me")
            .withCallAction((usedBot, context) -> {
                // todo:
                //  - вызывается команда ScrapperClient::registerChat(Long chatId)
                //  - аргументы: id ЧАТА!
                //  - исправить сообщение пользователю

                    usedBot.sendPlainTextMessage(context.getChatId(), "command 'start' was called");
                    LOGGER.info("...Command start called...");
                }
            );
    }

    public static TelegramBotCommand help() {
        return new TelegramBotCommand()
            .withCommandName("help")
            .withCommandTextDescription("get list of commands")
            .withCallAction((usedBot, context) -> {
                // todo:
                //  - исправить сообщение пользователю

                    usedBot.sendPlainTextMessage(context.getChatId(), "command 'help' was called");
                    LOGGER.info("...Command help called...");
                }
            );
    }

    public static TelegramBotCommand track(UsersRepository repository) {
        return new TelegramBotCommand()
            .withCommandName("track")
            .withCommandTextDescription("track links in arguments")
            .withOrderedArguments(
                CommandArgumentDescription.of(Link::validate, true)
            )
            .withCallAction((usedBot, context) -> {
                // todo:
                //  - вызывается команда ScrapperClient::trackLink(Long chatId, String link)
                //  - аргументы: id ЧАТА, ссылка для отслеживания
                //  - исправить сообщение пользователю

                    var userId = context.getUser().getTelegramId();
                    var links = context.getCommand().args();
                    repository.storeLinksForUser(userId, links);
                    usedBot.sendPlainTextMessage(
                        context.getChatId(),
                        "Tracked links:\n" + String.join("\n", links)
                    );
                    LOGGER.info("...Command track called...");
                }
            );
    }

    public static TelegramBotCommand untrack(UsersRepository repository) {
        return new TelegramBotCommand()
            .withCommandName("untrack")
            .withCommandTextDescription("untrack links in arguments")
            .withOrderedArguments(
                CommandArgumentDescription.of(Link::validate, true)
            )
            .withCallAction((usedBot, context) -> {
                // todo:
                //  - вызывается команда ScrapperClient::untrackLink(Long chatId, String link)
                //  - аргументы: id ЧАТА, ссылка для УДАЛЕНИЯ отслеживания
                //  - исправить сообщение пользователю

                    var userId = context.getUser().getTelegramId();
                    var links = context.getCommand().args();
                    repository.deleteLinksForUser(userId, links);
                    usedBot.sendPlainTextMessage(
                        context.getChatId(),
                        "Untracked links:\n" + String.join("\n", links)
                    );
                    LOGGER.info("...Command untrack called...");
                }
            );
    }

    public static TelegramBotCommand list(UsersRepository repository) {
        return new TelegramBotCommand()
            .withCommandName("list")
            .withCommandTextDescription("show tracked links")
            .withCallAction((usedBot, context) -> {
                // todo:
                //  - вызывается команда ScrapperClient::getAllTrackedLinks(Long chatId)
                //  - аргументы: id ЧАТА
                //  - исправить сообщение пользователю

                    var userId = context.getUser().getTelegramId();
                    var links = repository.getLinksForUser(userId);
                    usedBot.sendPlainTextMessage(
                        context.getChatId(),
                        "Tracked links is:\n" + String.join("\n", links)
                    );
                    LOGGER.info("...Command list called...");
                }
            );
    }

    public static TelegramBotCommand unexpectedCommand() {
        return new TelegramBotCommand()
            .withCallAction((usedBot, context) -> {
                    // todo:
                    //  - исправить сообщение пользователю

                    usedBot.sendPlainTextMessage(
                        context.getChatId(),
                        "Unexpected command or argument. Please, check out your query!"
                    );
                    LOGGER.info("...Unexpected command called...");
                }
            );
    }

    private final static Logger LOGGER = LogManager.getLogger();
}
