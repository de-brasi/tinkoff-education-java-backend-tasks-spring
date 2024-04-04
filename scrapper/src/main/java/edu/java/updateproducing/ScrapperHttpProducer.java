package edu.java.updateproducing;

import edu.common.datatypes.dtos.LinkUpdateRequest;
import edu.java.clients.BotClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class ScrapperHttpProducer implements ScrapperUpdateProducer {
    private final BotClient botClient;

    @Override
    public void send(LinkUpdateRequest linkUpdateRequest) {
        log.warn("Link update got: " + linkUpdateRequest);

        botClient.sendUpdates(linkUpdateRequest);
    }
}
