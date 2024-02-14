package edu.java.bot.interfaces;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import java.util.List;

public class LinkTracker implements UpdatesListener {
    private final TelegramBot bot;

    public LinkTracker(TelegramBot bot) {
        this.bot = bot;
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
