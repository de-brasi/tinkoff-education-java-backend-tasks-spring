package edu.java.updateproducing;

import edu.java.services.enteties.LinkUpdate;

public interface ScrapperUpdateProducer {
    void send(LinkUpdate linkUpdate);
}
