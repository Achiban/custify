package com.custify.model;

import com.custify.model.enums.TypeMessage;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "message")
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_msg")
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String contenu;

    @Column(nullable = false)
    private LocalDateTime horodatage;

    @Column(nullable = false)
    private Boolean lu = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TypeMessage type;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_canal", nullable = false)
    private Canal canal;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "expediteur", nullable = false)
    private Utilisateur expediteur;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destinataire")
    private Utilisateur destinataire;

    @PrePersist
    void prePersist() {
        if (horodatage == null) {
            horodatage = LocalDateTime.now();
        }
        if (lu == null) {
            lu = false;
        }
    }
}
