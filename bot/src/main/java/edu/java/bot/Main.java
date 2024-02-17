package edu.java.bot;

import com.pengrad.telegrambot.ExceptionHandler;
import com.pengrad.telegrambot.model.BotCommand;
import com.pengrad.telegrambot.model.botcommandscope.BotCommandScopeAllPrivateChats;
import com.pengrad.telegrambot.request.SetMyCommands;
import com.pengrad.telegrambot.response.BaseResponse;
import edu.java.bot.services.LinkTrackerObserver;
import edu.java.bot.services.LinkTrackerErrorHandler;
import edu.java.bot.services.TelegramBotWrapper;
import edu.java.bot.core.commands.ReadyToUseCommands;

// TODO: виды команд: с аргументами и без
//  Процесс создания контекста запроса:
//  1) бот получил сообщение;
//  2) сообщение - команда:
//      реагировать текстом пользователю (например написать "введите..."),
//      выйти из цепочки обработчиков
//  3) сообщение - какой текст (НЕ команда):
//      посмотреть предыдущее сообщение пользователя,
//      если это команда,
//          то создать объект контекста запроса и пустить по цепочке обработчиков;
//      если это НЕ команда,
//          то создать какой-то Faulty-контекст и пустить по цепочке обработчиков;

// todo:
//  1) получить предыдущее сообщение пользователя;
//  2) создать цепочку вызовов;
//  3) по цепочке вызовов составить контекст вызова;
//  4) передать контекст первому обработчику;
//  5) сделать функции-обвязки для отправки сообщений пользователю через бот
//      (избегание прямого использования функций SDK)

public class Main {
    public static void main(String[] args) {
        TelegramBotWrapper bot = new TelegramBotWrapper(
            System.getenv("TELEGRAM_TOKEN")
        );

        LinkTrackerObserver listener = new LinkTrackerObserver(bot);
        listener.setCommands(
            ReadyToUseCommands.unexpectedCommand(),
            ReadyToUseCommands.help(),
            ReadyToUseCommands.track(),
            ReadyToUseCommands.untrack(),
            ReadyToUseCommands.list()
        );

        ExceptionHandler errorHandler = new LinkTrackerErrorHandler();

        bot.setUpdatesListener(listener, errorHandler);

        System.out.println("i am here");
    }
}
