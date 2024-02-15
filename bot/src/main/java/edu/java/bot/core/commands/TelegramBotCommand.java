package edu.java.bot.core.commands;

import com.pengrad.telegrambot.TelegramBot;
import edu.java.bot.entities.CommandCallContext;
import lombok.Getter;
import java.util.Objects;

public class TelegramBotCommand {
    @Getter private String commandName;

    @Getter private String commandDescription;

    @Getter private TelegramBotCommandCallAction callAction;

    private TelegramBotCommand nextCommand;

    // TODO: написать предупреждение об избегании циклов в цепочке команд,
    //  но по-хорошему эту цепочку формирует мой же код (а не настройка бота)

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

    public void setNextCommand(TelegramBotCommand nextCommand) {
        this.nextCommand = nextCommand;
    }

    public void handle(TelegramBot handlerTelegramBot, CommandCallContext context) {
        // todo: если аргумент соответствует критериям для этого обработчика, то вызвать callAction(bot);
        //  если не такая команда или недостаточно аргументов, или аргументы неправильные,
        //  то вызвать следующий обработчик

        callAction.call(handlerTelegramBot);
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
