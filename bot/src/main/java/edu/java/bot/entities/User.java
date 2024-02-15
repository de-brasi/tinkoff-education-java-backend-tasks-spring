package edu.java.bot.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter public class User {
    private String firstName;
    private String lastName;
    private int telegramId;
}
