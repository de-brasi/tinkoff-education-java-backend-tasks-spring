package edu.java.services.jpa;

import edu.java.domain.entities.Link;
import edu.java.domain.repositories.jpa.entities.TelegramChat;
import edu.java.domain.repositories.jpa.entities.TrackInfo;
import edu.java.domain.repositories.jpa.implementations.JpaLinkRepository;
import edu.java.domain.repositories.jpa.implementations.JpaTelegramChatRepository;
import edu.java.services.interfaces.LinkService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class JpaLinkService implements LinkService {
    public JpaLinkService(
        @Autowired JpaLinkRepository jpaLinkRepository,
        @Autowired JpaTelegramChatRepository jpaTelegramChatRepository
    ) {
        this.linkRepository = jpaLinkRepository;
        this.chatRepository = jpaTelegramChatRepository;
    }

    @PersistenceContext
    private EntityManager entityManager;

    private final JpaLinkRepository linkRepository;
    private final JpaTelegramChatRepository chatRepository;

    @Override
    @Transactional
    public Link add(long tgChatId, URI url) {
        try {
            TelegramChat chat = chatRepository.get(tgChatId);
            edu.java.domain.repositories.jpa.entities.Link link = linkRepository.get(url.toURL().toString());
            TrackInfo trackInfo = new TrackInfo();
            trackInfo.setLink(link);
            trackInfo.setChat(chat);
            entityManager.persist(trackInfo);
            entityManager.flush();
            return new Link(url);
        } catch (MalformedURLException e) {
            // todo
            throw new RuntimeException(e);
        }
    }

    @Override
    @Transactional
    public Link remove(long tgChatId, URI url) {
        try {
            TelegramChat chat = chatRepository.get(tgChatId);
            edu.java.domain.repositories.jpa.entities.Link link = linkRepository.get(url.toURL().toString());
            TrackInfo trackInfo = new TrackInfo();
            trackInfo.setLink(link);
            trackInfo.setChat(chat);
            entityManager.remove(trackInfo);
            entityManager.flush();
            return new Link(url);
        } catch (NoResultException e) {
            return null;
        } catch (MalformedURLException e) {
            // todo
            throw new RuntimeException(e);
        }
    }

    @Override
    @Transactional
    public Collection<Link> listAll(long tgChatId) {

        List<TrackInfo> trackInfos = entityManager.createQuery(
                "SELECT ti FROM TrackInfo ti WHERE ti.chat.id = :tgChatId", TrackInfo.class)
            .setParameter("tgChatId", tgChatId)
            .getResultList();

        return trackInfos.stream()
            .map(
                e -> new Link(
                    URI.create(e.getLink().getUrl())
                )
            )
            .collect(Collectors.toList());
    }
}
