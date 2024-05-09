package edu.java.data.domain.repositories.jpa.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "links")
@NoArgsConstructor
@Getter
@Setter
public class Link {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String url;

    @Column(nullable = false)
    private OffsetDateTime lastCheckTime;

    @Column(nullable = false)
    private OffsetDateTime lastUpdateTime;

    @ManyToOne
    @JoinColumn(name = "service", referencedColumnName = "id")
    private SupportedService service;

    private String snapshot;
}
