package edu.java.bot;

import static org.assertj.core.api.Assertions.*;
import edu.java.bot.core.commands.CommandArgumentDescription;
import edu.java.bot.core.commands.CommandArgumentValidator;
import edu.java.bot.core.commands.TelegramBotCommand;
import edu.java.bot.core.commands.TelegramBotCommandCallAction;
import edu.java.bot.entities.Command;
import edu.java.bot.entities.CommandCallContext;
import edu.java.bot.entities.User;
import edu.java.bot.services.LinkTrackerObserver;
import edu.java.bot.services.TelegramBotWrapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;
import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@ExtendWith(MockitoExtension.class)
public class TelegramBotCommandTest {
    @Mock TelegramBotWrapper telegramBot;

    @Test
    @DisplayName("Test setting name with withCommandName method")
    public void test1() {
        TelegramBotCommand testingCommand = new TelegramBotCommand();
        final String usedName = "test";

        testingCommand = testingCommand.withCommandName(usedName);

        assertThat(usedName).isEqualTo(testingCommand.getCommandName());
    }

    @Test
    @DisplayName("Test setting description with withCommandTextDescription method")
    public void test2() {
        TelegramBotCommand testingCommand = new TelegramBotCommand();
        final String usedDescription = "test";

        testingCommand = testingCommand.withCommandTextDescription(usedDescription);

        assertThat(usedDescription).isEqualTo(testingCommand.getCommandDescription());
    }

    @Test
    @DisplayName("Test setting call action")
    public void test3() throws NoSuchFieldException, IllegalAccessException {
        TelegramBotCommand testingCommand = new TelegramBotCommand();
        final TelegramBotCommandCallAction usedAction =
            (bot, context) -> bot.sendPlainTextMessage(-1L, "hi");

        testingCommand = testingCommand.withCallAction(usedAction);

        Field callActionField = TelegramBotCommand.class.getDeclaredField("callAction");
        callActionField.setAccessible(true);

        TelegramBotCommandCallAction actualAction = (TelegramBotCommandCallAction) callActionField.get(testingCommand);

        assertThat(usedAction).isEqualTo(actualAction);
    }

    @Test
    @DisplayName("Test setting next command")
    public void test4() throws NoSuchFieldException, IllegalAccessException {
        TelegramBotCommand testedCommand = new TelegramBotCommand();
        final TelegramBotCommand expectedNextCommand =
            new TelegramBotCommand()
                .withCallAction(
                    (targetBot, context) -> {}
                );

        testedCommand.setNextCommand(expectedNextCommand);
        Field nextCommandField = TelegramBotCommand.class.getDeclaredField("nextCommand");
        nextCommandField.setAccessible(true);

        var actualNextCommand = (TelegramBotCommand) nextCommandField.get(testedCommand);

        assertThat(actualNextCommand).isEqualTo(expectedNextCommand);
    }

    @Test
    @DisplayName("Test handling suitable context")
    public void test5() {
        final String testCommandName = "some-command";
        final String testCommandArg1 = "arg1";
        final String testCommandArg2 = "arg2";

        final String testUserName = "test-name";
        final String testUserLastName = "test-last-name";
        final Long testUserId = -1L;
        final Long testChatId = 0L;

        AtomicInteger actualSuitableArgsCount = new AtomicInteger();
        final int expectedSuitableArgsCount = 2;

        final CommandArgumentValidator testValidator =
            (String arg) -> arg.equals(testCommandArg1) || arg.equals(testCommandArg2);

        // setup command
        TelegramBotCommand testedBotCommand =
            new TelegramBotCommand()
            .withCommandName(testCommandName)
            .withOrderedArguments(
                CommandArgumentDescription.of(testValidator, true)
            )
            .withCallAction(
                (targetBot, context) -> actualSuitableArgsCount.incrementAndGet()
            );

        final Command calledCommandByTestUser1 = new Command(
            testCommandName, List.of(testCommandArg1)
        );
        final Command calledCommandByTestUser2 = new Command(
            testCommandName, List.of(testCommandArg2)
        );

        CommandCallContext suitableContext1 = new CommandCallContext(
            new User(testUserName, testUserLastName, testUserId, false),
            testChatId,
            calledCommandByTestUser1
        );

        CommandCallContext suitableContext2 = new CommandCallContext(
            new User(testUserName, testUserLastName, testUserId, false),
            testChatId,
            calledCommandByTestUser2
        );

        assertThatCode(() -> testedBotCommand.handle(telegramBot, suitableContext1)).doesNotThrowAnyException();
        assertThatCode(() -> testedBotCommand.handle(telegramBot, suitableContext2)).doesNotThrowAnyException();
        assertThat(actualSuitableArgsCount.get()).isEqualTo(expectedSuitableArgsCount);
    }

    @Test
    @DisplayName("Test handling faulty context")
    public void test6() {
        final String testCommandName = "some-command";
        final String suitableCommandArg = "arg";
        final String faultyCommandArg = "faulty-arg";

        final String testUserName = "test-name";
        final String testUserLastName = "test-last-name";
        final Long testUserId = -1L;
        final Long testChatId = 0L;

        AtomicBoolean handlerTerminatorCalledActualValue = new AtomicBoolean(false);
        final boolean handlerTerminatorCalledExpectedValue = true;

        final CommandArgumentValidator testValidator = (String arg) -> arg.equals(suitableCommandArg);

        // setup command-terminator
        TelegramBotCommand commandTerminator =
            new TelegramBotCommand()
                .withCallAction(
                    (targetBot, context) -> {
                        handlerTerminatorCalledActualValue.set(true);
                    }
                );

        // setup main command
        TelegramBotCommand testedBotCommand =
            new TelegramBotCommand()
                .withCommandName(testCommandName)
                .withOrderedArguments(
                    CommandArgumentDescription.of(testValidator, true)
                )
                .withCallAction(
                    (targetBot, context) -> {
                        throw new RuntimeException("Not expected to reach it");
                    }
                );
        testedBotCommand.setNextCommand(commandTerminator);

        final Command calledFaultyCommandByTestUser = new Command(
            testCommandName, List.of(faultyCommandArg)
        );

        CommandCallContext faultyContext = new CommandCallContext(
            new User(testUserName, testUserLastName, testUserId, false),
            testChatId,
            calledFaultyCommandByTestUser
        );

        assertThatCode(() -> testedBotCommand.handle(telegramBot, faultyContext)).doesNotThrowAnyException();
        assertThat(handlerTerminatorCalledActualValue.get()).isEqualTo(handlerTerminatorCalledExpectedValue);
    }
}
