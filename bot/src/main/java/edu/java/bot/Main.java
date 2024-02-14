package edu.java.bot;

import com.pengrad.telegrambot.ExceptionHandler;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.BotCommand;
import com.pengrad.telegrambot.model.botcommandscope.BotCommandScopeAllPrivateChats;
import com.pengrad.telegrambot.request.SetMyCommands;
import com.pengrad.telegrambot.response.BaseResponse;
import edu.java.bot.core.LinkTracker;
import edu.java.bot.core.LinkTrackerErrorHandler;
import edu.java.bot.core.TelegramBotWrapper;

public class Main {
    public static void main(String[] args) {
        // TODO: перед тем как закомитить УДАЛИ ТОКЕН !!!!
        TelegramBotWrapper bot = new TelegramBotWrapper(

        );

        // Set commands
        BotCommand allChatsHiCommand = new BotCommand("hi", "get me 'hi' message");
        System.out.println("command is: " + allChatsHiCommand.command());
        BotCommand allChatsGetMeGreetingCommand = new BotCommand("greeting", "get me greeting message");

        SetMyCommands setCommandsRequest =
            new SetMyCommands(
                allChatsHiCommand,
                allChatsGetMeGreetingCommand
            ).scope(new BotCommandScopeAllPrivateChats());
        BaseResponse response = bot.execute(setCommandsRequest);

        System.out.println("Setting commands response: " + response.description());

        UpdatesListener listener = new LinkTracker(bot);
        ExceptionHandler errorHandler = new LinkTrackerErrorHandler();

        bot.setUpdatesListener(listener, errorHandler);

        System.out.println("i am here");
    }
}
