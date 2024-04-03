package edu.java.updateproducing;

import edu.java.clients.BotClient;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ScrapperHttpProducer implements ScrapperUpdateProducer {
    private final String topicName;
    private final BotClient botClient;

    @Override
    public void send(String someMessage) {
        // TODO: topic name
        // todo: использовать id ссылки, пока заглушка

        System.out.println("заглушка");

        botClient.sendUpdates(-1, topicName, someMessage, null);
    }
}
