package edu.java.services.jdbc;

import edu.java.entities.Link;
import edu.java.services.interfaces.LinkService;
import java.net.URI;
import java.util.Collection;

public class JdbcLinkService implements LinkService {
    @Override
    public Link add(long tgChatId, URI url) {
        return null;
    }

    @Override
    public Link remove(long tgChatId, URI url) {
        return null;
    }

    @Override
    public Collection<Link> listAll(long tgChatId) {
        return null;
    }
}
