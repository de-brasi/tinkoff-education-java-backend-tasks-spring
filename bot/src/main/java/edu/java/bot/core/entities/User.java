package edu.java.bot.core.entities;

import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter public class User {
    private String firstName;
    private String lastName;
    private Long telegramId;
    private boolean isBot;

    @Override public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        User user = (User) o;
        return isBot == user.isBot
            && Objects.equals(firstName, user.firstName)
            && Objects.equals(lastName, user.lastName)
            && Objects.equals(telegramId, user.telegramId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(firstName, lastName, telegramId, isBot);
    }
}
