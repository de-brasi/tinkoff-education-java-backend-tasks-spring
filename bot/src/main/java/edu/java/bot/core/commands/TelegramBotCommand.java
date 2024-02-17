package edu.java.bot.core.commands;

import edu.java.bot.entities.Command;
import edu.java.bot.entities.CommandCallContext;
import edu.java.bot.services.TelegramBotWrapper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.Getter;

// TODO: добавить текстовое описание что делает класс.
//  Важно указать, что имеется поддержка только команд формата "/command args..",
//  то есть команда и некоторое число аргументов.
//  Поддержки "диалога" с различными ветками в зависимости от ответа пользователя нет -
//  бот (возможно) будет опрашивать пользователя на предмет получения аргументов,
//  пока не получит аргументы, удовлетворяющие условию (СОЗДАТЬ ВАЛИДАТОР АРГУМЕНТОВ).
//  Лишние для введенной команды аргументы отбрасываются.

@SuppressWarnings("MultipleStringLiterals")
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

    public TelegramBotCommand withOrderedArguments(CommandArgumentDescription... descriptions) {
        validateArgumentsDescription(descriptions);

        this.orderedArgumentsDescription = new ArrayList<>(List.of(descriptions));
        return this;
    }

    public void setNextCommand(TelegramBotCommand nextCommand) {
        this.nextCommand = nextCommand;
    }

    public void handle(TelegramBotWrapper handlerTelegramBot, CommandCallContext context) {
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

    private void validateArgumentsDescription(CommandArgumentDescription... descriptions) {
        var gotDescription = new ArrayList<>(List.of(descriptions));
        long countOfNotOnlyOneArgDescriptors = gotDescription
            .stream()
            .filter(CommandArgumentDescription::isTrailingAndNotOne)
            .count();

        if (gotDescription.getLast().isTrailingAndNotOne() && countOfNotOnlyOneArgDescriptors > 1) {
            throw new RuntimeException(
                "Too many descriptions of argument with unknown quantity"
                + ((this.commandName != null && !this.commandName.isEmpty())
                    ? String.format("in setting signature for command %s. ", this.commandName)
                    : ". ")
                + "Remember that you can only specify a description for one such argument, "
                + "and it must be the last one in order."
            );
        }
    }

    private boolean checkThisHandlerIsTerminator() {
        return this.nextCommand == null;
    }

    private boolean checkContextIsSuitable(CommandCallContext context) {
        return context.getCommand().name().equals(this.commandName);
    }

    private void handleSuitableContext(TelegramBotWrapper handlerTelegramBot, CommandCallContext context) {
        var sievedArguments = sieveAllArguments(context.getCommand().args());

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

    @SuppressWarnings("ReturnCount")
    private Optional<List<String>> sieveAllArguments(List<String> args) {
        ArrayList<String> sourceArgs = new ArrayList<>(args);
        List<String> sieved = new ArrayList<>(List.of());

        // No arguments allowed in command description - just discard all arguments.
        if (orderedArgumentsDescription == null || orderedArgumentsDescription.isEmpty()) {
            return Optional.of(sieved);
        }

        // Check count of expected arguments and count got.
        if (orderedArgumentsDescription.size() > sourceArgs.size()) {
            return Optional.empty();
        }

        // Test all arguments except last - last can describe restriction on several trailing arguments.
        for (int i = 0; i < orderedArgumentsDescription.size() - 1; i++) {
            var currentDescriptor = orderedArgumentsDescription.get(i);
            var currentTestedArg = sourceArgs.get(i);
            var validationResult = sieveArgumentsValuesWithOneValidator(currentDescriptor, currentTestedArg);

            if (validationResult.isPresent()) {
                sieved.addAll(validationResult.get());
            } else {
                return Optional.empty();
            }
        }

        String[] trailingArguments = sourceArgs
            .subList(orderedArgumentsDescription.size() - 1, sourceArgs.size())
            .toArray(String[]::new);

        // Test trailing arguments with validator in last descriptor.
        var currentDescriptor = orderedArgumentsDescription.getLast();
        var validationResult = sieveArgumentsValuesWithOneValidator(currentDescriptor, trailingArguments);

        if (validationResult.isPresent() && !validationResult.get().isEmpty()) {
            sieved.addAll(validationResult.get());
        } else {
            return Optional.empty();
        }

        return Optional.of(sieved);
    }

    private Optional<List<String>> sieveArgumentsValuesWithOneValidator(
        CommandArgumentDescription curValidator,
        String... toValidate
    ) {
        if (toValidate.length == 0) {
            return Optional.empty();
        }

        List<String> sieved = Arrays.stream(toValidate).filter(curValidator.validator()::validate).toList();
        return Optional.of(sieved);
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
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TelegramBotCommand that = (TelegramBotCommand) o;
        return Objects.equals(commandName, that.commandName)
            && Objects.equals(commandDescription, that.commandDescription)
            && Objects.equals(callAction, that.callAction);
    }

    @Override
    public int hashCode() {
        return Objects.hash(commandName, commandDescription, callAction);
    }
}
