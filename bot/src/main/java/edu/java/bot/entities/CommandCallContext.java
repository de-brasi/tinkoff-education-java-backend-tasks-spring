package edu.java.bot.entities;

import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter public class CommandCallContext {
    private User user;
    private Long chatId;
    private Command command;

    @Override public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CommandCallContext that = (CommandCallContext) o;
        return Objects.equals(user, that.user)
            && Objects.equals(chatId, that.chatId)
            && Objects.equals(command, that.command);
    }

    @Override
    public int hashCode() {
        return Objects.hash(user, chatId, command);
    }
}
