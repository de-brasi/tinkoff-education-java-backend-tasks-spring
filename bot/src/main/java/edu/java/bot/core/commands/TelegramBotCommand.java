package edu.java.bot.core.commands;

import edu.java.bot.core.TelegramBotWrapper;
import edu.java.bot.entities.Command;
import edu.java.bot.entities.CommandCallContext;
import lombok.Getter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

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
        // todo: проверять, что есть не более одного CommandArgumentDescription с флагом isTrailingAndNotOne,
        //  и если такое есть, то последним элементом
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

        if (context == null) {
            throw new RuntimeException("Unexpected to handling empty context!");
        }

        if (checkThisHandlerIsTerminator()) {
            callAction.call(handlerTelegramBot, context);
        } else if (checkContextIsSuitable(context)) {
            handleSuitableContext(handlerTelegramBot, context);
        } else {
            nextCommand.handle(handlerTelegramBot, context);
        }
    }

    private boolean checkThisHandlerIsTerminator() {
        return this.nextCommand == null;
    }

    private boolean checkContextIsSuitable(CommandCallContext context) {
        return context.getCommand().name().equals(this.commandName);
    }

    private void handleSuitableContext(TelegramBotWrapper handlerTelegramBot, CommandCallContext context) {
        var sievedArguments = getSievedArguments(context.getCommand().args());

        if (sievedArguments.isPresent()) {
            CommandCallContext puredContext = new CommandCallContext(
                context.getUser(),
                context.getChatId(),
                new Command(context.getCommand().name(), sievedArguments.get())
                );
            callAction.call(handlerTelegramBot, puredContext);
        } else {
            // Faulty context, not suitable for every handler.
            // Expected handling this context in the end of handler's chain by termination handler.
            nextCommand.handle(handlerTelegramBot, context);
        }
    }

    private Optional<List<String>> getSievedArguments(List<String> args) {
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

        ArrayList<String> sourceArgs = new ArrayList<>(args);
        List<String> sieved = new ArrayList<>(List.of());

        if (args.isEmpty()) {
            return Optional.of(sieved);
        }

        // test all arguments except last
        // todo: уменьшить сложность кода
        for (int i = 0; i < orderedArgumentsDescription.size() - 1; i++) {
            // для всех кроме последней пары аргумент-валидатор
            if (sourceArgs.size() > i) {
                // если соответствующий аргумент существует
                var validationResult = sieveWithOneValidator(
                    orderedArgumentsDescription.get(i),
                    sourceArgs.get(i)
                );

                if (validationResult.isPresent()) {
                    sieved.addAll(validationResult.get());
                } else {
                    // пустой Optional - некорректный набор элементов
                    return Optional.empty();
                }

            } else {
                // faulty arguments set
                return Optional.empty();
            }
        }

        // test with last validator
        // протестить последний валидатор и аргумент(ы) для него
        if (orderedArgumentsDescription.getLast().isTrailingAndNotOne()) {
            // если последний аргумент описан как опциональный,
            // то если какие то аргументы были отброшены (пусть даже все) - пофек
            // todo: протестировать когда набор аргументов пуст

            var res = sieveWithOneValidator(
                orderedArgumentsDescription.getLast(),
                sourceArgs.get(orderedArgumentsDescription.size() - 1)
            );

            res.ifPresent(sieved::addAll);
        } else if (sourceArgs.size() >= orderedArgumentsDescription.size()) {
            // аргумент описан как обязательный
            // надо проверить что для него существует какое-то значение в переданных аргументах
            // todo: тут проверить что элемент существует и он подходит

            var validationResult = sieveWithOneValidator(
                orderedArgumentsDescription.getLast(),
                sourceArgs.get(orderedArgumentsDescription.size() - 1)
            );

            // если прошло валидацию, то добавить в результат, иначе - набор аргументов невалидный
            if (validationResult.isPresent()) {
                sieved.addAll(validationResult.get());
            } else {
                return Optional.empty();
            }

        } else {
            // аргумент описан как обязательный, но значения для него нет
            // faulty arguments set
            return Optional.empty();
        }

        return Optional.of(sieved);
    }

    private Optional<List<String>> sieveWithOneValidator(CommandArgumentDescription curValidator, String... toValidate) {
        if (toValidate.length == 0) {
            // No elements toValidate in case of LAST OPTIONAL validator
            return Optional.of(List.of());
        }

        List<String> sieved = Arrays.stream(toValidate)
            .filter(e -> curValidator.validator().validate(e))
            .toList();

        if (curValidator.isTrailingAndNotOne()) {
            return Optional.of(sieved);
        } else {
            // Validator not optional (in common sense) when it tests only one element from arguments.
            // If it was discarded by filter when element is faulty.
            return (sieved.size() == toValidate.length) ? Optional.of(sieved) : Optional.empty();
        }
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
