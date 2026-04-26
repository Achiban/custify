package com.custify.model;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "participant_canal")
public class ParticipantCanal {

    @EmbeddedId
    private ParticipantCanalId id = new ParticipantCanalId();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("canalId")
    @JoinColumn(name = "id_canal", nullable = false)
    private Canal canal;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("utilisateurId")
    @JoinColumn(name = "id_user", nullable = false)
    private Utilisateur utilisateur;

    @Column(name = "rejoint_le", nullable = false)
    private LocalDateTime rejointLe;

    @Column(nullable = false)
    private Boolean actif = true;

    @PrePersist
    void prePersist() {
        if (rejointLe == null) {
            rejointLe = LocalDateTime.now();
        }
        if (actif == null) {
            actif = true;
        }
    }
}
