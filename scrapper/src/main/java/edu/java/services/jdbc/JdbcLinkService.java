package edu.java.services.jdbc;

import edu.common.exceptions.ChatIdNotExistsException;
import edu.common.exceptions.IncorrectRequestException;
import edu.common.exceptions.ReAddingLinkException;
import edu.java.domain.BaseEntityRepository;
import edu.java.domain.JdbcChatLinkBoundRepository;
import edu.java.domain.entities.ChatLinkBound;
import edu.java.domain.entities.Link;
import edu.java.domain.exceptions.DataBaseInteractingException;
import edu.java.domain.exceptions.InvalidArgumentForTypeInDataBase;
import edu.java.domain.exceptions.NoExpectedEntityInDataBaseException;
import edu.java.services.interfaces.LinkService;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.Collection;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class JdbcLinkService implements LinkService {
    private final BaseEntityRepository<ChatLinkBound> linkBoundRepository;

    public JdbcLinkService(@Autowired JdbcChatLinkBoundRepository linkBoundRepository) {
        this.linkBoundRepository = linkBoundRepository;
    }

    @Override
    @Transactional
    public Link add(long tgChatId, URI url) {
        ChatLinkBound bound;

        try {
            bound = new ChatLinkBound(tgChatId, url.toURL().toString());
        } catch (MalformedURLException e) {
            throw new InvalidArgumentForTypeInDataBase(e);
        }

        try {
            boolean res = linkBoundRepository.add(bound);

            if (!res) {
                throw new ReAddingLinkException();
            }

            return new Link(url);
        } catch (InvalidArgumentForTypeInDataBase e) {
            throw new IncorrectRequestException(e);
        } catch (NoExpectedEntityInDataBaseException e) {
            throw new ChatIdNotExistsException(e);
        } catch (DataAccessException | NullPointerException e) {
            throw new DataBaseInteractingException(e);
        }
    }

    @Override
    @Transactional
    public Link remove(long tgChatId, URI url) {

        ChatLinkBound bound;
        try {
            bound = new ChatLinkBound(tgChatId, url.toURL().toString());
        } catch (MalformedURLException e) {
            throw new InvalidArgumentForTypeInDataBase(e);
        }

        try {
            Optional<ChatLinkBound> removed = linkBoundRepository.remove(bound);
            return removed
                .map(chatLinkBound -> new Link(
                    URI.create(chatLinkBound.linkURL())
                ))
                .orElse(null);
        } catch (InvalidArgumentForTypeInDataBase e) {
            throw new IncorrectRequestException(e);
        } catch (NoExpectedEntityInDataBaseException e) {
            throw new ChatIdNotExistsException(e);
        }
    }

    @Override
    @Transactional
    public Collection<Link> listAll(long tgChatId) {
        // todo: изменить интерфейс BaseEntityRepository,
        //  чтобы он как-то поддерживал выбор значений по предикату(?)
        Collection<ChatLinkBound> allBounds = linkBoundRepository.findAll();
        return allBounds
            .stream()
            .map(e -> new Link(URI.create(e.linkURL())))
            .toList();
    }
}
