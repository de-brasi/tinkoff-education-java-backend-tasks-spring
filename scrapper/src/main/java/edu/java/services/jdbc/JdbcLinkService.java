package edu.java.services.jdbc;

import edu.common.exceptions.ChatIdNotExistsException;
import edu.common.exceptions.IncorrectRequestException;
import edu.common.exceptions.ReAddingLinkException;
import edu.java.domain.entities.ChatLinkBound;
import edu.java.domain.entities.Link;
import edu.java.domain.exceptions.DataBaseInteractingException;
import edu.java.domain.exceptions.InvalidArgumentForTypeInDataBase;
import edu.java.domain.exceptions.NoExpectedEntityInDataBaseException;
import edu.java.domain.repositories.jdbc.JdbcChatLinkBoundRepository;
import edu.java.domain.repositories.jdbc.JdbcLinkRepository;
import edu.java.services.interfaces.LinkService;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.Collection;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@SuppressWarnings("MultipleStringLiterals")
public class JdbcLinkService implements LinkService {

    private final JdbcChatLinkBoundRepository linkBoundRepository;
    private final JdbcLinkRepository linkRepository;

    @Override
    @Transactional
    public Link add(long tgChatId, URI url) {
        try {
            ChatLinkBound bound;
            final String urlString = url.toURL().toString();
            bound = new ChatLinkBound(tgChatId, urlString);

            // add
            final int createdRecordsCount = linkBoundRepository.add(bound);
            final int expectedCreatedRecordsCount = 1;

            if (createdRecordsCount != expectedCreatedRecordsCount) {
                throw new ReAddingLinkException();
            }

            // get id of link that may be just created
            final Long addedRecordId = linkRepository.getEntityId(urlString);

            if (addedRecordId < 0L) {
                // unexpected state
                throw new NoExpectedEntityInDataBaseException(
                    "Expected record for url %s not found".formatted(urlString)
                );
            }

            return new Link(addedRecordId, url);
        } catch (InvalidArgumentForTypeInDataBase e) {
            throw new IncorrectRequestException(e);
        } catch (NoExpectedEntityInDataBaseException e) {
            throw new ChatIdNotExistsException(e);
        } catch (DataAccessException | NullPointerException e) {
            throw new DataBaseInteractingException(e);
        } catch (MalformedURLException e) {
            throw new InvalidArgumentForTypeInDataBase(e);
        }
    }

    @Override
    @Transactional
    public Link remove(long tgChatId, URI url) {
        try {
            final String urlString = url.toURL().toString();

            ChatLinkBound bound = new ChatLinkBound(tgChatId, urlString);

            // get id of link
            final Long addedRecordId = linkRepository.getEntityId(urlString);

            if (addedRecordId < 0) {
                // unexpected state
                throw new NoExpectedEntityInDataBaseException(
                    "Expected record for url %s not found".formatted(urlString)
                );
            }

            // remove
            final int removedRecordsCount = linkBoundRepository.remove(bound);
            final int expectedRemovedRecordsCount = 1;

            if (removedRecordsCount != expectedRemovedRecordsCount) {
                return null;
            }

            return new Link(addedRecordId, url);
        } catch (InvalidArgumentForTypeInDataBase e) {
            throw new IncorrectRequestException(e);
        } catch (NoExpectedEntityInDataBaseException e) {
            throw new ChatIdNotExistsException(e);
        } catch (MalformedURLException e) {
            throw new InvalidArgumentForTypeInDataBase(e);
        }
    }

    @Override
    @Transactional
    public Collection<Link> listAll(long tgChatId) {
        return linkBoundRepository.getLinksTrackedBy(tgChatId);
    }
}
