package edu.java.bot.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import java.util.List;

@AllArgsConstructor
@Getter public class CommandCallContext {
    private User user;
    private Long chatId;
    private String command;
    private List<String> commandArgs;
}
