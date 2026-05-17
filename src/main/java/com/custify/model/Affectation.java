package com.custify.model;

import com.custify.model.enums.StatutAffectation;
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
@Table(name = "affectation")
public class Affectation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_affectation")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_commercial", nullable = false)
    private Utilisateur commercial;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_beneficiaire", nullable = false)
    private Utilisateur clientBeneficiaire;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_opp", nullable = false)
    private Opportunite opportunite;

    @Column(name = "date_affectation", nullable = false)
    private LocalDateTime dateAffectation;

    @Column(name = "message_commercial", columnDefinition = "TEXT")
    private String messageCommercial;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut_client", nullable = false, length = 30)
    private StatutAffectation statutClient = StatutAffectation.EN_ATTENTE;

    @PrePersist
    void prePersist() {
        if (dateAffectation == null) {
            dateAffectation = LocalDateTime.now();
        }
    }
}
