package edu.java.domain.repositories.jpa.implementations;

import edu.java.domain.repositories.jpa.entities.TrackInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import java.util.Collection;

public interface JpaTrackInfoRepository extends JpaRepository<TrackInfo, Long> {
    @Transactional(readOnly = true)
    Collection<TrackInfo> findTrackInfoByChat_Id(Long chatId);

    @Transactional
    @Modifying
    @Query(value = "INSERT INTO track_info(telegram_chat_id, link_id) VALUES (:chatId, :linkId) ON CONFLICT DO NOTHING ",
           nativeQuery = true)
    void saveIfNotExists(Long chatId, Long linkId);

    @Transactional
    default void saveIfNotExists(TrackInfo trackInfo) {
        saveIfNotExists(trackInfo.getChat().getId(), trackInfo.getLink().getId());
    }
}
