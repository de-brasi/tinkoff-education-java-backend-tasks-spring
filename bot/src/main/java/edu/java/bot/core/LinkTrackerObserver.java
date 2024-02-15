package edu.java.bot.core;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.BotCommand;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.request.SetMyCommands;
import com.pengrad.telegrambot.response.BaseResponse;
import com.pengrad.telegrambot.response.SendResponse;
import edu.java.bot.core.commands.TelegramBotCommand;
import edu.java.bot.entities.CommandCallContext;
import edu.java.bot.entities.User;
import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// TODO:
//  - настроить поведение прихода нового пользователя (команда /start ?);
//  - документация методов;
//  - создать мапперы для получения Entities из объектов Update

// todo (что отразить в документации):
//  Задокументировать, что данные по цепочке ответственности,
//  если не могут быть выполнены текущим обработчиком,
//  всегда переходят к следующему элементу
//  (то есть доходят до терминального обработчика, который уже выполняется);
//  Так же надо указать, что обязательно должна быть команда-терминатор.
//  Обычных команд может не быть, но терминатор быть должен.

// todo (порядок обработки одного Update): получать update,
//  если НЕ сообщение (а как же первый заход пользователя?), то игнорировать;
//  создавать цепочку сообщений по ответам,
//      пока сообщение от клиента не будет без ответа (клиент <- бот <- ... <- клиент);
//  обрабатывать цепочку (как?) - создавать контекст команды

public class LinkTrackerObserver implements UpdatesListener {
    private final TelegramBot bot;

    @Nullable private TelegramBotCommand handlersChainHead;
    @Nullable private TelegramBotCommand handlersChainTail;

    public LinkTrackerObserver(TelegramBot bot) {
        this.bot = bot;
    }

    public void configureCommands(
        TelegramBotCommand terminationCommand,
        TelegramBotCommand... commandsChain
    ) {
        ArrayList<TelegramBotCommand> gotCommands = new ArrayList<>(Arrays.stream(commandsChain).toList());
        registerCommandsForTelegramBot(gotCommands);

        // configure chain of handlers
        ArrayList<TelegramBotCommand> allHandlersInChainOfResponsibility = new ArrayList<>(gotCommands);
        allHandlersInChainOfResponsibility.add(terminationCommand);
        makeHandlersChain(allHandlersInChainOfResponsibility);
    }

    @Override
    public int process(List<Update> list) {
        for (Update update: list) {
            // todo: better logging
            System.out.println("New update: " + update);

            // TODO: разобраться с контентом update - что там может быть
            if (!checkUpdateContainsNewMessage(update)) {
                continue;
            }

            Chat curChat = update.message().chat();
            Object chatId = curChat.id();

            // todo: вынести в отдельную функцию проверки
            if (this.handlersChainHead == null) {
                SendMessage request = new SendMessage(chatId, "Sorry! This bot is not available now :(");
                bot.execute(request);
                throw new RuntimeException(
                    "Chain of handlers not configured or configured with failure. handlersChainHead is null!"
                );
            }

            CommandCallContext callContext = makeCallContext(update);
            this.handlersChainHead.handle(bot, callContext);

            // todo: log got message to user
            {
                String messageReceived = update.message().text();

                // send message
                SendMessage request = new SendMessage(chatId, "handled message, with text " + messageReceived);
                SendResponse sendResponse = bot.execute(request);

                Message message = sendResponse.message();
                System.out.println("message: " + message.text());
                System.out.println("response: " + sendResponse);
            }
        }

        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

    private void makeHandlersChain(ArrayList<TelegramBotCommand> gotCommands) {
        assert !gotCommands.isEmpty();

        this.handlersChainHead = gotCommands.get(0);
        this.handlersChainTail = gotCommands.get(0);

        for (int i = 1; i < gotCommands.size(); i++) {
            this.handlersChainTail.setNextCommand(gotCommands.get(i));
            this.handlersChainTail = gotCommands.get(i);
        }
    }

    private void registerCommandsForTelegramBot(List<TelegramBotCommand> commands) {
        BotCommand[] botCommands = commands.stream()
            .map(command -> new BotCommand(command.getCommandName(), command.getCommandDescription()))
            .toArray(BotCommand[]::new);

        for (var command :
            botCommands) {
            System.out.println(command);
        }

        SetMyCommands myCommands = new SetMyCommands(botCommands);
        BaseResponse response = this.bot.execute(myCommands);

        if (!response.isOk()) {
            throw new RuntimeException("Failure when set commands for bot");
        } else {
            // todo: better logging
            System.out.println("Response to commands configuration: " + response);
        }
    }

    private boolean checkUpdateContainsNewMessage(Update update) {
        return (update != null) && (update.message() != null);
    }

    private CommandCallContext makeCallContext(Update updateObj) {
        User sender = new User(
            updateObj.message().from().firstName(),
            updateObj.message().from().lastName(),
            updateObj.message().from().id(),
            updateObj.message().from().isBot()
        );

        Chat curChat = updateObj.message().chat();
        Long chatId = curChat.id();

        String failureCommandLabel = "";
        String command = failureCommandLabel;
        String[] separatedMessageContent = updateObj.message().text().split(" ");

        // if command like "/command" exists and command's body not empty
        if (separatedMessageContent.length > 0 && separatedMessageContent[0].length() > 1) {
            String firstWordInMessage = separatedMessageContent[0];
            command = firstWordInMessage.startsWith("/")
                ? firstWordInMessage.substring(1)
                : failureCommandLabel;
        }

        List<String> arguments = Arrays.stream(separatedMessageContent).skip(1).toList();
        return new CommandCallContext(sender, chatId, command, arguments);
    }
}
