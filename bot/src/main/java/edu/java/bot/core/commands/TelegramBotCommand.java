package edu.java.bot.core.commands;

import lombok.Getter;
import java.util.Objects;

@Getter public class TelegramBotCommand {
    private String commandName;

    private String commandDescription;

    private TelegramBotCommandCallAction callAction;

    // TODO: ссылку на следующую команду (TelegramBotCommand) в цепочке

    public TelegramBotCommand() {}

    public TelegramBotCommand withCommandName(String commandName) {
        this.commandName = commandName;
        return this;
    }

    public TelegramBotCommand withCommandTextDescription(String commandDescription) {
        this.commandDescription = commandDescription;
        return this;
    }

    public TelegramBotCommand withCallAction(TelegramBotCommandCallAction callAction) {
        this.callAction = callAction;
        return this;
    }

    @Override
    public String toString() {
        return String.format(
            "TelegramBotCommand obj with name=%s, description=%s, action=%s",
            (this.commandName != null && !this.commandName.isEmpty())
                ? this.commandName
                : "<not configured>",
            (this.commandDescription != null && !this.commandDescription.isEmpty())
                ? this.commandDescription
                : "<not configured>",
            this.callAction != null
                ? this.callAction.toString()
                : "<not configured>"
        );
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TelegramBotCommand that = (TelegramBotCommand) o;
        return Objects.equals(commandName, that.commandName) &&
            Objects.equals(commandDescription, that.commandDescription) &&
            Objects.equals(callAction, that.callAction);
    }

    @Override
    public int hashCode() {
        return Objects.hash(commandName, commandDescription, callAction);
    }
}
