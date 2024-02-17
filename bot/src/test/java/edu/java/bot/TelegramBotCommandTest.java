package edu.java.bot;

import static org.assertj.core.api.Assertions.*;
import edu.java.bot.core.commands.TelegramBotCommand;
import edu.java.bot.core.commands.TelegramBotCommandCallAction;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import java.lang.reflect.Field;

public class TelegramBotCommandTest {
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
    @DisplayName("Test setting setting call action")
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
    @DisplayName("Test handling suitable context")
    public void test4() {
        //  todo
    }

    @Test
    @DisplayName("Test handling faulty context")
    public void test5() throws NoSuchFieldException, IllegalAccessException {
        //  todo
    }
}
