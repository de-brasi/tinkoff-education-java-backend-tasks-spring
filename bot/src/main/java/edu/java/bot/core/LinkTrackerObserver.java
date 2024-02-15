package edu.java.bot.core;

import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.BotCommand;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SetMyCommands;
import com.pengrad.telegrambot.response.BaseResponse;
import edu.java.bot.TelegramBotWrapper;
import edu.java.bot.core.commands.TelegramBotCommand;
import edu.java.bot.core.mappers.FromPengradTelegramBotModelsToEntitiesMapper;
import edu.java.bot.entities.CommandCallContext;
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
    private final TelegramBotWrapper bot;

    @Nullable private TelegramBotCommand handlersChainHead;
    @Nullable private TelegramBotCommand handlersChainTail;

    public LinkTrackerObserver(TelegramBotWrapper bot) {
        this.bot = bot;
    }

    public void setCommands(
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

            if (!checkUpdateContainsNewMessage(update)) {
                continue;
            }

            Chat curChat = update.message().chat();
            Long chatId = curChat.id();

            if (!verifyHandlersChain()) {
                bot.sendPlainTextMessage(chatId,"Sorry! This bot is not available now :(");
                throw new RuntimeException(
                    "Chain of handlers not configured or configured with failure. handlersChainHead is null!"
                );
            }

            CommandCallContext callContext =
                FromPengradTelegramBotModelsToEntitiesMapper.updateToCommandCallContext(update);
            this.handlersChainHead.handle(bot, callContext);
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

    private boolean verifyHandlersChain() {
        return this.handlersChainHead != null;
    }
}
