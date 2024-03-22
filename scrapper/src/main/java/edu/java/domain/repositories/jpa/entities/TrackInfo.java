package edu.java.domain.repositories.jpa.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "track_info")
@NoArgsConstructor
public class TrackInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "telegram_chat_id", referencedColumnName = "id")
    private TelegramChat chat;

    @ManyToOne
    @JoinColumn(name = "link_id", referencedColumnName = "id")
    private Link link;
}
