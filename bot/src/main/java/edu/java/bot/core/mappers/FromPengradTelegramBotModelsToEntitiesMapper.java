package edu.java.bot.core.mappers;

import com.pengrad.telegrambot.model.Update;
import edu.java.bot.core.entities.Command;
import edu.java.bot.core.entities.CommandCallContext;
import edu.java.bot.core.entities.User;
import java.util.Arrays;
import java.util.List;

public class FromPengradTelegramBotModelsToEntitiesMapper {
    private FromPengradTelegramBotModelsToEntitiesMapper() {}

    private final static String FAILURE_COMMAND_LABEL = "";
    private final static String REQUEST_ITEMS_SEPARATOR = " ";
    private final static String TELEGRAM_COMMAND_PREFIX = "/";


    public static Command updateToCommand(Update src) {
        String commandName = FAILURE_COMMAND_LABEL;
        String[] separatedMessageContent = src.message().text().split(REQUEST_ITEMS_SEPARATOR);

        // if command like "/command" exists and command's body not empty
        if (separatedMessageContent.length > 0 && separatedMessageContent[0].length() > 1) {
            String firstWordInMessage = separatedMessageContent[0];
            commandName = firstWordInMessage.startsWith(TELEGRAM_COMMAND_PREFIX)
                ? firstWordInMessage.substring(1)
                : FAILURE_COMMAND_LABEL;
        }
        List<String> arguments = Arrays.stream(separatedMessageContent).skip(1).toList();

        return new Command(commandName, arguments);
    }

    public static User updateToSender(Update src) {
        com.pengrad.telegrambot.model.User fromUser = src.message().from();
        return new User(
            fromUser.firstName(),
            fromUser.lastName(),
            fromUser.id(),
            fromUser.isBot()
        );
    }

    public static CommandCallContext updateToCommandCallContext(Update src) {
        User sender = updateToSender(src);
        Long chatId = src.message().chat().id();
        Command command = updateToCommand(src);
        return new CommandCallContext(sender, chatId, command);
    }
}
