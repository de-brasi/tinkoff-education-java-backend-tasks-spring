package edu.java.bot.coretest;

import edu.java.bot.api.UpdateController;
import edu.java.bot.api.util.UpdateHandler;
import edu.java.bot.client.ScrapperClient;
import edu.java.bot.configuration.KafkaConfig;
import edu.java.bot.core.commands.TelegramBotCommand;
import edu.java.bot.core.entities.Command;
import edu.java.bot.core.entities.CommandCallContext;
import edu.java.bot.core.entities.User;
import edu.java.bot.services.TelegramBotService;
import edu.java.bot.services.TelegramBotWrapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import java.util.List;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
public class ReadyToUseCommandsTest {
    @MockBean
    TelegramBotWrapper telegramBot;

    @MockBean
    ScrapperClient scrapperClient;

    @MockBean
    UpdateController updateController;

    @MockBean
    UpdateHandler updateHandler;

    @MockBean
    TelegramBotService telegramBotService;

    @MockBean
    KafkaConfig kafkaConfig;

    @Autowired
    @Qualifier("commandStart")
    TelegramBotCommand commandStart;

    @Autowired
    @Qualifier("commandHelp")
    TelegramBotCommand commandHelp;

    @Autowired
    @Qualifier("commandTrack")
    TelegramBotCommand commandTrack;

    @Autowired
    @Qualifier("commandUntrack")
    TelegramBotCommand commandUntrack;

    @Autowired
    @Qualifier("commandList")
    TelegramBotCommand commandList;

    @Autowired
    @Qualifier("unexpectedCommand")
    TelegramBotCommand unexpectedCommand;

    private final TelegramBotCommand voidTerminationCommand =
        new TelegramBotCommand()
            .withCallAction(((targetBot, context) -> {}));

    private final User testSender = new User("test", "sender", -1L, false);

    @Test
    @DisplayName("Test calling command 'track' with one valid argument")
    public void test1() {
        // setup handlers chain
        TelegramBotCommand testedCommand = commandTrack;
        testedCommand.setNextCommand(voidTerminationCommand);

        // make context
        final Command testTrackCommandWithOneValidLink =
            new Command("track", List.of("https://ru.wikipedia.org/wiki/"));
        final CommandCallContext testContextWithOneValidLink = new CommandCallContext(
            testSender,
            -1L,
            testTrackCommandWithOneValidLink
            );

        testedCommand.handle(telegramBot, testContextWithOneValidLink);
        verify(telegramBot, times(1))
            .sendPlainTextMessage(
                ArgumentMatchers.anyLong(),
                ArgumentMatchers.anyString()
            );
    }

    @Test
    @DisplayName("Test calling command 'track' with more than one valid argument")
    public void test2() {
        // setup handlers chain
        TelegramBotCommand testedCommand = commandTrack;
        testedCommand.setNextCommand(voidTerminationCommand);

        // make context
        final Command testTrackCommandWithOneValidLink =
            new Command(
                "track",
                List.of("https://ru.wikipedia.org/wiki/", "https://en.wikipedia.org/wiki/")
            );
        final CommandCallContext testContextWithOneValidLink = new CommandCallContext(
            testSender,
            -1L,
            testTrackCommandWithOneValidLink
        );

        testedCommand.handle(telegramBot, testContextWithOneValidLink);
        verify(telegramBot, times(1))
            .sendPlainTextMessage(
                ArgumentMatchers.anyLong(),
                ArgumentMatchers.anyString()
            );
    }

    @Test
    @DisplayName("Test calling command 'track' without any argument")
    public void test3() {
        // setup handlers chain
        TelegramBotCommand testedCommand = commandTrack;
        testedCommand.setNextCommand(voidTerminationCommand);

        // make context
        final Command testTrackCommandWithOneValidLink =
            new Command("track", List.of());
        final CommandCallContext testContextWithOneValidLink = new CommandCallContext(
            testSender,
            -1L,
            testTrackCommandWithOneValidLink
        );

        testedCommand.handle(telegramBot, testContextWithOneValidLink);
        verify(telegramBot, times(0))
            .sendPlainTextMessage(
                ArgumentMatchers.anyLong(),
                ArgumentMatchers.anyString()
            );
    }

    @Test
    @DisplayName("Test calling command 'track' with one invalid argument")
    public void test4() {
        // setup handlers chain
        TelegramBotCommand testedCommand = commandTrack;
        testedCommand.setNextCommand(voidTerminationCommand);

        // make context
        final Command testTrackCommandWithOneValidLink =
            new Command("track", List.of("https://seems-like-not-a-valid-url/endpoint"));
        final CommandCallContext testContextWithOneValidLink = new CommandCallContext(
            testSender,
            -1L,
            testTrackCommandWithOneValidLink
        );

        testedCommand.handle(telegramBot, testContextWithOneValidLink);
        verify(telegramBot, times(0))
            .sendPlainTextMessage(
                ArgumentMatchers.anyLong(),
                ArgumentMatchers.anyString()
            );
    }

    @Test
    @DisplayName("Test calling command 'track' with more than one invalid argument")
    public void test5() {
        // setup handlers chain
        TelegramBotCommand testedCommand = commandTrack;
        testedCommand.setNextCommand(voidTerminationCommand);

        // make context
        final Command testTrackCommandWithOneValidLink =
            new Command(
                "track",
                List.of(
                    "https://seems-like-not-a-valid-url/endpoint1",
                    "https://en.wikiped"
                )
            );
        final CommandCallContext testContextWithOneValidLink = new CommandCallContext(
            testSender,
            -1L,
            testTrackCommandWithOneValidLink
        );

        testedCommand.handle(telegramBot, testContextWithOneValidLink);
        verify(telegramBot, times(0))
            .sendPlainTextMessage(
                ArgumentMatchers.anyLong(),
                ArgumentMatchers.anyString()
            );
    }

    @Test
    @DisplayName("Test calling command 'track' with one valid argument and one invalid argument")
    public void test6() {
        /*
        In this case valid url must be accepted by handler.
        The reason is it possible case when client add a lot of URLs and all will be declined because of one wrong.
        */

        // setup handlers chain
        TelegramBotCommand testedCommand = commandTrack;
        testedCommand.setNextCommand(voidTerminationCommand);

        // make context
        final Command testTrackCommandWithOneValidLink =
            new Command(
                "track",
                List.of(
                    "https://seems-like-not-a-valid-url/endpoint1",
                    "https://en.wikipedia.org/"
                )
            );
        final CommandCallContext testContextWithOneValidLink = new CommandCallContext(
            testSender,
            -1L,
            testTrackCommandWithOneValidLink
        );

        testedCommand.handle(telegramBot, testContextWithOneValidLink);
        verify(telegramBot, times(1))
            .sendPlainTextMessage(
                ArgumentMatchers.anyLong(),
                ArgumentMatchers.anyString()
            );
    }

    @Test
    @DisplayName("Test calling command 'untrack' with one valid argument")
    public void test7() {
        // setup handlers chain
        TelegramBotCommand testedCommand = commandUntrack;
        testedCommand.setNextCommand(voidTerminationCommand);

        // make context
        final Command testTrackCommandWithOneValidLink =
            new Command("untrack", List.of("https://ru.wikipedia.org/wiki/"));
        final CommandCallContext testContextWithOneValidLink = new CommandCallContext(
            testSender,
            -1L,
            testTrackCommandWithOneValidLink
        );

        testedCommand.handle(telegramBot, testContextWithOneValidLink);
        verify(telegramBot, times(1))
            .sendPlainTextMessage(
                ArgumentMatchers.anyLong(),
                ArgumentMatchers.anyString()
            );
    }

    @Test
    @DisplayName("Test calling command 'untrack' with more than one valid argument")
    public void test8() {
        // setup handlers chain
        TelegramBotCommand testedCommand = commandUntrack;
        testedCommand.setNextCommand(voidTerminationCommand);

        // make context
        final Command testTrackCommandWithOneValidLink =
            new Command(
                "untrack",
                List.of("https://ru.wikipedia.org/wiki/", "https://en.wikipedia.org/wiki/")
            );
        final CommandCallContext testContextWithOneValidLink = new CommandCallContext(
            testSender,
            -1L,
            testTrackCommandWithOneValidLink
        );

        testedCommand.handle(telegramBot, testContextWithOneValidLink);
        verify(telegramBot, times(1))
            .sendPlainTextMessage(
                ArgumentMatchers.anyLong(),
                ArgumentMatchers.anyString()
            );
    }

    @Test
    @DisplayName("Test calling command 'untrack' without any argument")
    public void test9() {
        // setup handlers chain
        TelegramBotCommand testedCommand = commandUntrack;
        testedCommand.setNextCommand(voidTerminationCommand);

        // make context
        final Command testTrackCommandWithOneValidLink =
            new Command("untrack", List.of());
        final CommandCallContext testContextWithOneValidLink = new CommandCallContext(
            testSender,
            -1L,
            testTrackCommandWithOneValidLink
        );

        testedCommand.handle(telegramBot, testContextWithOneValidLink);
        verify(telegramBot, times(0))
            .sendPlainTextMessage(
                ArgumentMatchers.anyLong(),
                ArgumentMatchers.anyString()
            );
    }

    @Test
    @DisplayName("Test calling command 'untrack' with one invalid argument")
    public void test10() {
        // setup handlers chain
        TelegramBotCommand testedCommand = commandUntrack;
        testedCommand.setNextCommand(voidTerminationCommand);

        // make context
        final Command testTrackCommandWithOneValidLink =
            new Command("untrack", List.of("https://seems-like-not-a-valid-url/endpoint"));
        final CommandCallContext testContextWithOneValidLink = new CommandCallContext(
            testSender,
            -1L,
            testTrackCommandWithOneValidLink
        );

        testedCommand.handle(telegramBot, testContextWithOneValidLink);
        verify(telegramBot, times(0))
            .sendPlainTextMessage(
                ArgumentMatchers.anyLong(),
                ArgumentMatchers.anyString()
            );
    }

    @Test
    @DisplayName("Test calling command 'untrack' with more than one invalid argument")
    public void test11() {
        // setup handlers chain
        TelegramBotCommand testedCommand = commandUntrack;
        testedCommand.setNextCommand(voidTerminationCommand);

        // make context
        final Command testTrackCommandWithOneValidLink =
            new Command(
                "untrack",
                List.of(
                    "https://seems-like-not-a-valid-url/endpoint1",
                    "https://en.wikiped"
                )
            );
        final CommandCallContext testContextWithOneValidLink = new CommandCallContext(
            testSender,
            -1L,
            testTrackCommandWithOneValidLink
        );

        testedCommand.handle(telegramBot, testContextWithOneValidLink);
        verify(telegramBot, times(0))
            .sendPlainTextMessage(
                ArgumentMatchers.anyLong(),
                ArgumentMatchers.anyString()
            );
    }

    @Test
    @DisplayName("Test calling command 'untrack' with one valid argument and one invalid argument")
    public void test12() {
         /*
        In this case valid url must be accepted by handler.
        The reason is it possible case when client add a lot of URLs and all will be declined because of one wrong.
        */

        // setup handlers chain
        TelegramBotCommand testedCommand = commandUntrack;
        testedCommand.setNextCommand(voidTerminationCommand);

        // make context
        final Command testTrackCommandWithOneValidLink =
            new Command(
                "untrack",
                List.of(
                    "https://seems-like-not-a-valid-url/endpoint1",
                    "https://en.wikipedia.org/"
                )
            );
        final CommandCallContext testContextWithOneValidLink = new CommandCallContext(
            testSender,
            -1L,
            testTrackCommandWithOneValidLink
        );

        testedCommand.handle(telegramBot, testContextWithOneValidLink);
        verify(telegramBot, times(1))
            .sendPlainTextMessage(
                ArgumentMatchers.anyLong(),
                ArgumentMatchers.anyString()
            );
    }
}
