package edu.java.bot.core.commands;

import edu.java.bot.TelegramBotWrapper;
import edu.java.bot.entities.CommandCallContext;
import lombok.Getter;
import java.util.ArrayList;
import java.util.List;
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

    private ArrayList<CommandArgumentDescription> orderedArgumentsDescription;

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

    public TelegramBotCommand withOrderedArguments(CommandArgumentDescription... description) {
        this.orderedArgumentsDescription = new ArrayList<>(List.of(description));
        return this;
    }

    public void setNextCommand(TelegramBotCommand nextCommand) {
        this.nextCommand = nextCommand;
    }

    public void handle(TelegramBotWrapper handlerTelegramBot, CommandCallContext context) {
        // TODO:
        //  проверять правильность аргументов (по валидаторам),
        //  проверять число аргументов (по числу валидаторов),
        //  учесть случай когда аргументов неизвестное число
        //  (например пользователь кидает неограниченное число ссылок для отслеживиания)

        // TODO:
        //  сделать функцию для валидации данных
        //  исходы:
        //      1) все поля обязательные и единичные
        //          -> вернуть Optional на набор аргументов если все валидны,
        //          -> пустой Optional если какое то поле не валидно
        //      2) последний аргумент необязательный и неединичный
        //          -> вернуть Optional на набор аргументов если все валидны
        //              (последние аргументы можно не учитывать),
        //          -> пустой Optional если какое то поле (кроме последнего) не валидно

        if (context == null) {
            throw new RuntimeException("Unexpected to handling empty context!");
        }

        if (checkThisHandlerIsTerminator()) {
            callAction.call(handlerTelegramBot, context);
        } else if (context.getCommand().name().equals(this.commandName)) {
            callAction.call(handlerTelegramBot, context);
        } else {
            nextCommand.handle(handlerTelegramBot, context);
        }
    }

    private boolean checkThisHandlerIsTerminator() {
        return this.nextCommand == null;
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
