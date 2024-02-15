package edu.java.bot.core.commands;

import com.pengrad.telegrambot.TelegramBot;
import edu.java.bot.entities.CommandCallContext;
import lombok.Getter;
import java.util.Objects;

// TODO: добавить текстовое описание что делает класс.
//  Важно указать, что имеется поддержка только команд формата "/command args..",
//  то есть команда и некоторое число аргументов.
//  Поддержки "диалога" с различными ветками в зависимости от ответа пользователя нет -
//  бот (возможно) будет опрашивать пользователя на предмет получения аргументов,
//  пока не получит аргументы, удовлетворяющие условию (СОЗДАТЬ ВАЛИДАТОР АРГУМЕНТОВ).
//  Лишние для введенной команды аргументы отбрасываются.
public class TelegramBotCommand {
    @Getter private String commandName;

    @Getter private String commandDescription;

    @Getter private TelegramBotCommandCallAction callAction;

    private TelegramBotCommand nextCommand;

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
