package edu.java.data.services.interfaces;

import edu.java.data.domain.entities.Link;
import java.net.URI;
import java.util.Collection;

public interface LinkService {
    Link add(long tgChatId, URI url);

    Link remove(long tgChatId, URI url);

    Collection<Link> listAll(long tgChatId);
}
