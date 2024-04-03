package edu.java.updateproducing;

import edu.java.clients.BotClient;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ScrapperHttpProducer implements ScrapperUpdateProducer {
    @Value("#{@kafkaTopic}")
    private String topicName;
    private final BotClient botClient;

    @Override
    public void send(String someMessage) {
        // TODO: topic name
        // todo: использовать id ссылки, пока заглушка

        System.out.println("заглушка");

        botClient.sendUpdates(-1, topicName, someMessage, null);
    }
}
