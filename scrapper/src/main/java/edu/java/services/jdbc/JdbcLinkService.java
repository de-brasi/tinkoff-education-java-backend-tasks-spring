package edu.java.services.jdbc;

import edu.common.exceptions.ChatIdNotExistsException;
import edu.common.exceptions.IncorrectRequestException;
import edu.common.exceptions.ReAddingLinkException;
import edu.java.domain.repositories.BaseEntityRepository;
import edu.java.domain.repositories.jdbc.JdbcChatLinkBoundRepository;
import edu.java.domain.entities.ChatLinkBound;
import edu.java.domain.entities.Link;
import edu.java.domain.entities.TelegramChat;
import edu.java.domain.exceptions.InvalidArgumentForTypeInDataBase;
import edu.java.domain.exceptions.NoExpectedEntityInDataBaseException;
import edu.java.services.interfaces.LinkService;
import java.net.URI;
import java.util.Collection;
import org.springframework.beans.factory.annotation.Autowired;
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
        TelegramChat chat = new TelegramChat(tgChatId);
        Link link = new Link(url);
        ChatLinkBound bound = new ChatLinkBound(chat, link);

        try {
            boolean res = linkBoundRepository.add(bound);

            if (!res) {
                throw new ReAddingLinkException();
            }

            return link;
        } catch (InvalidArgumentForTypeInDataBase e) {
            throw new IncorrectRequestException(e);
        } catch (NoExpectedEntityInDataBaseException e) {
            throw new ChatIdNotExistsException(e);
        }
    }

    @Override
    @Transactional
    public Link remove(long tgChatId, URI url) {
        TelegramChat chat = new TelegramChat(tgChatId);
        Link link = new Link(url);
        ChatLinkBound bound = new ChatLinkBound(chat, link);

        try {
            ChatLinkBound removed = linkBoundRepository.remove(bound);
            return (removed == null) ? null : removed.link();
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
            .map(ChatLinkBound::link)
            .toList();
    }
}
