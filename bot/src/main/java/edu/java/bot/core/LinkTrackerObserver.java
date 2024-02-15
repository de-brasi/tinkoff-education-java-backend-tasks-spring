package edu.java.bot.core;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import edu.java.bot.core.commands.TelegramBotCommand;
import java.util.List;

public class LinkTrackerObserver implements UpdatesListener {
    private final TelegramBot bot;

    private TelegramBotCommand handlersChainHead;

    public LinkTrackerObserver(TelegramBot bot) {
        this.bot = bot;
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

        // регистрация команд

        // создание цепочки
    }

    @Override
    public int process(List<Update> list) {
        for (Update update: list) {
            // TODO:
            //  реализовать тут же команды - чтобы на /start без аргументов отсылать какое-то сообщение

            // log handling update
            System.out.println("New update: " + update.toString());

            if (update.message() == null) {
                continue;
            }

            // TODO: разобраться с контентом update - что там может быть
            Chat curChat = update.message().chat();
            if (curChat != null) {
                Object chatId = curChat.id();

                // got message
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
}
