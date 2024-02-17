package edu.java.bot;

import static org.assertj.core.api.Assertions.*;
import edu.java.bot.core.commands.ReadyToUseCommands;
import edu.java.bot.core.commands.TelegramBotCommand;
import edu.java.bot.entities.Command;
import edu.java.bot.entities.CommandCallContext;
import edu.java.bot.entities.User;
import edu.java.bot.repository.implementations.UserRepositoryMockImpl;
import edu.java.bot.repository.interfaces.UsersRepository;
import edu.java.bot.services.TelegramBotWrapper;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import java.util.List;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ReadyToUseCommandsTest {
    @Mock TelegramBotWrapper telegramBot;

    private final UsersRepository mockRepo = new UserRepositoryMockImpl();

    private final TelegramBotCommand voidTerminationCommand =
        new TelegramBotCommand()
            .withCallAction(((targetBot, context) -> {}));

    private final User testSender = new User("test", "sender", -1L, false);

    @Test
    @DisplayName("Test calling command 'track' with one valid argument")
    public void test1() {
        // setup handlers chain
        TelegramBotCommand testedCommand = ReadyToUseCommands.track(mockRepo);
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
        TelegramBotCommand testedCommand = ReadyToUseCommands.track(mockRepo);
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
        TelegramBotCommand testedCommand = ReadyToUseCommands.track(mockRepo);
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
        TelegramBotCommand testedCommand = ReadyToUseCommands.track(mockRepo);
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
        TelegramBotCommand testedCommand = ReadyToUseCommands.track(mockRepo);
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
        TelegramBotCommand testedCommand = ReadyToUseCommands.track(mockRepo);
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
        TelegramBotCommand testedCommand = ReadyToUseCommands.untrack(mockRepo);
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
        TelegramBotCommand testedCommand = ReadyToUseCommands.untrack(mockRepo);
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
        TelegramBotCommand testedCommand = ReadyToUseCommands.untrack(mockRepo);
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
        TelegramBotCommand testedCommand = ReadyToUseCommands.untrack(mockRepo);
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
        TelegramBotCommand testedCommand = ReadyToUseCommands.untrack(mockRepo);
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
        TelegramBotCommand testedCommand = ReadyToUseCommands.untrack(mockRepo);
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
