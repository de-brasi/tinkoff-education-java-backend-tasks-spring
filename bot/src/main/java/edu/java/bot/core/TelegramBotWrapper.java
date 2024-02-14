package edu.java.bot.core;

import com.pengrad.telegrambot.TelegramBot;
import edu.java.bot.core.commands.TelegramBotCommand;

public class TelegramBotWrapper extends TelegramBot {
    public TelegramBotWrapper(String botToken) {
        super(botToken);
    }

    public void configureCommandsChain(
        TelegramBotCommand terminationCommand,
        TelegramBotCommand... commandsChain
    ) {
        // TODO: Собрать тут цепочку ответственности, в самом конце выставить команду-терминатор.
        //  Задокументировать, что данные по цепочке ответственности,
        //  если не могут быть выполнены текущим обработчиком,
        //  всегда переходят к следующему элементу
        //  (то есть доходят до терминального обработчика, который уже выполняется);
        //  Так же надо указать, что обязательно должна быть команда-терминатор.
        //  Обычных команд может не быть, но терминатор быть должен.
    }
}
