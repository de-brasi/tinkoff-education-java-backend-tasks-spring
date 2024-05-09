package edu.java.data.domain.repositories.jpa.entities;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "track_info")
@NoArgsConstructor
@Getter
@Setter
public class TrackInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "telegram_chat_id", referencedColumnName = "id")
    private TelegramChat chat;

    @ManyToOne
    @JoinColumn(name = "link_id", referencedColumnName = "id")
    private Link link;
}
