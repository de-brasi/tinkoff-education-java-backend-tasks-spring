package edu.java.updateproducing;

import edu.java.clients.BotClient;
import edu.java.services.enteties.LinkUpdate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class ScrapperHttpProducer implements ScrapperUpdateProducer {
    private final BotClient botClient;

    @Override
    public void send(LinkUpdate linkUpdate) {
        log.warn("Link update got: " + linkUpdate);

        botClient.sendUpdates(
            linkUpdate.linkId(),
            linkUpdate.url(),
            linkUpdate.updateDescription(),
            linkUpdate.subscribers()
        );
    }
}
