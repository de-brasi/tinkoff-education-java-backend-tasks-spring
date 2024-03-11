package edu.java.bot;

import static org.assertj.core.api.Assertions.*;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.User;
import edu.java.bot.core.mappers.FromPengradTelegramBotModelsToEntitiesMapper;
import edu.java.bot.entities.Command;
import edu.java.bot.entities.CommandCallContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FromPengradTelegramBotModelsToEntitiesMapperTest {
    @Mock
    Update updateFromServer;

    @Mock
    Message message;

    @Mock
    User user;

    @Mock
    Chat chat;

    @Test
    @DisplayName("Test mapping from Update to Command")
    public void test1() {
        final String testCommandName = "testcommand";
        final String testArgsString = "testcommand";
        final List<String> testArgs = List.of(testArgsString.split(" "));

        // setup message
        when(message.text()).thenReturn(String.format("/%s %s", testCommandName, testArgsString));

        // setup update
        when(updateFromServer.message()).thenReturn(message);

        Command actual = FromPengradTelegramBotModelsToEntitiesMapper.updateToCommand(updateFromServer);
        Command expected = new Command("testcommand", testArgs);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    @DisplayName("Test mapping from Update to User")
    public void test2() {
        final String testUserName = "test";
        final String testUserLastName = "user";
        final Long testUserId = 1L;
        final boolean testUserIsBot = false;

        // setup user
        when(user.firstName()).thenReturn(testUserName);
        when(user.lastName()).thenReturn(testUserLastName);
        when(user.id()).thenReturn(testUserId);
        when(user.isBot()).thenReturn(testUserIsBot);

        // setup message
        when(message.from()).thenReturn(user);

        // setup update
        when(updateFromServer.message()).thenReturn(message);

        edu.java.bot.entities.User actual =
            FromPengradTelegramBotModelsToEntitiesMapper.updateToSender(updateFromServer);
        edu.java.bot.entities.User expected =
            new edu.java.bot.entities.User(testUserName, testUserLastName, testUserId, testUserIsBot);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    @DisplayName("Test mapping from Update to CommandCallContext")
    public void test3() {
        final String testUserName = "test";
        final String testUserLastName = "user";
        final Long testUserId = 1L;
        final boolean testUserIsBot = false;
        final Long testChatId = 1L;
        final String testCommandName = "testcommand";
        final String testArgsString = "testcommand";
        final List<String> testArgs = List.of(testArgsString.split(" "));

        // setup update
        when(updateFromServer.message()).thenReturn(message);

        // setup user
        when(user.firstName()).thenReturn(testUserName);
        when(user.lastName()).thenReturn(testUserLastName);
        when(user.id()).thenReturn(testUserId);
        when(user.isBot()).thenReturn(testUserIsBot);

        // setup message
        when(message.from()).thenReturn(user);
        when(message.chat()).thenReturn(chat);
        when(message.text()).thenReturn(String.format("/%s %s", testCommandName, testArgsString));

        // setup chat
        when(chat.id()).thenReturn(testChatId);

        // setup update
        when(updateFromServer.message()).thenReturn(message);

        CommandCallContext actual =
            FromPengradTelegramBotModelsToEntitiesMapper.updateToCommandCallContext(updateFromServer);
        CommandCallContext expected = new CommandCallContext(
            new edu.java.bot.entities.User(testUserName, testUserLastName, testUserId, testUserIsBot),
            testChatId,
            new Command("testcommand", testArgs)
        );

        assertThat(actual).isEqualTo(expected);
    }
}
