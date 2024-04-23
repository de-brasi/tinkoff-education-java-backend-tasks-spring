package edu.java.domain.repositories.jpa.implementations;

import edu.java.domain.repositories.jpa.entities.TrackInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import java.util.Collection;

public interface JpaTrackInfoRepository extends JpaRepository<TrackInfo, Long> {
    @Transactional(readOnly = true)
    Collection<TrackInfo> findTrackInfoByChat_Id(Long chatId);
}
