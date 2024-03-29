package edu.java.bot.coretest;

import static org.assertj.core.api.Assertions.*;
import edu.java.bot.core.entities.Command;
import edu.java.bot.core.entities.CommandCallContext;
import edu.java.bot.core.entities.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class CommandCallContextTest {
    @Mock
    Command mockCommand;

    @Mock
    User mockUser;

    @Test
    @DisplayName("Test all args constructor exists")
    public void test1() {
        assertThatCode(() -> new CommandCallContext(mockUser, 0L, mockCommand)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Test getters")
    public void test2() {
        assertThatCode(() -> {
            var context = new CommandCallContext(mockUser, 0L, mockCommand);
            var command = context.getCommand();
            var user = context.getUser();
            var chatId = context.getChatId();
        }).doesNotThrowAnyException();
    }
}
