package edu.java.bot.services;

import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.BotCommand;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.botcommandscope.BotCommandScopeAllPrivateChats;
import com.pengrad.telegrambot.request.SetMyCommands;
import com.pengrad.telegrambot.response.BaseResponse;
import edu.java.bot.core.commands.TelegramBotCommand;
import edu.java.bot.core.customexceptions.InvalidHandlersChainException;
import edu.java.bot.core.entities.CommandCallContext;
import edu.java.bot.core.mappers.FromPengradTelegramBotModelsToEntitiesMapper;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public class LinkTrackerObserver implements UpdatesListener {
    private final TelegramBotWrapper bot;

    @Nullable private TelegramBotCommand handlersChainHead;
    @Nullable private TelegramBotCommand handlersChainTail;
    private final Counter counter;

    public LinkTrackerObserver(TelegramBotWrapper bot, MeterRegistry registry) {
        this.bot = bot;
        counter = Counter.builder("handled_messages").description("Count of messages handled by Telegram bot")
            .register(registry);
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
            LOGGER.info("New update: " + update);

            if (!checkUpdateContainsNewMessage(update)) {
                continue;
            }

            Chat currentChat = update.message().chat();
            Long chatId = currentChat.id();

            if (!verifyHandlersChain()) {
                bot.sendPlainTextMessage(chatId, "Sorry! This bot is not available now :(");
                throw new InvalidHandlersChainException(
                    "Chain of handlers not configured or configured with failure. handlersChainHead is null!"
                );
            }

            CommandCallContext callContext =
                FromPengradTelegramBotModelsToEntitiesMapper.updateToCommandCallContext(update);
            this.handlersChainHead.handle(bot, callContext);

            counter.increment();
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
            .map(
                command -> new BotCommand(
                    command.getCommandName(),
                    (command.getCommandDescription() != null)
                        ? command.getCommandDescription()
                        : command.getCommandName()
                )
            )
            .toArray(BotCommand[]::new);

        for (var command: botCommands) {
            LOGGER.info("Command get:" + command);
        }

        SetMyCommands myCommands = new SetMyCommands(botCommands).scope(new BotCommandScopeAllPrivateChats());
        BaseResponse response = this.bot.execute(myCommands);

        if (!response.isOk()) {
            throw new RuntimeException("Failure when set commands for bot: " + response);
        } else {
            LOGGER.info("Response to settings commands for bot: " + response);
        }
    }

    private boolean checkUpdateContainsNewMessage(Update update) {
        return (update != null) && (update.message() != null);
    }

    private boolean verifyHandlersChain() {
        return this.handlersChainHead != null;
    }

    private final static Logger LOGGER = LogManager.getLogger();
}
