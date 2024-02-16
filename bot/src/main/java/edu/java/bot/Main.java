package edu.java.bot;

import com.pengrad.telegrambot.ExceptionHandler;
import edu.java.bot.core.LinkTrackerObserver;
import edu.java.bot.core.LinkTrackerErrorHandler;
import edu.java.bot.core.TelegramBotWrapper;
import edu.java.bot.core.commands.ReadyForUseCommands;

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
        // TODO: перед тем как закомитить УДАЛИ ТОКЕН !!!!
        TelegramBotWrapper bot = new TelegramBotWrapper(

        );

        LinkTrackerObserver listener = new LinkTrackerObserver(bot);
        listener.setCommands(
            ReadyForUseCommands.unexpectedCommand(),
            ReadyForUseCommands.help(),
            ReadyForUseCommands.track(),
            ReadyForUseCommands.untrack(),
            ReadyForUseCommands.list()
        );

        ExceptionHandler errorHandler = new LinkTrackerErrorHandler();

        bot.setUpdatesListener(listener, errorHandler);

        System.out.println("i am here");
    }
}
