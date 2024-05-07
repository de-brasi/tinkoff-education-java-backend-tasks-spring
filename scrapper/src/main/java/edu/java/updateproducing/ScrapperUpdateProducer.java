package edu.java.updateproducing;

import edu.common.datatypes.dtos.LinkUpdateRequest;

public interface ScrapperUpdateProducer {
    void send(LinkUpdateRequest linkUpdate);
}
