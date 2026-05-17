package com.custify.model;

import com.custify.model.enums.StatutOpportunite;
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
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "opportunite")
public class Opportunite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_opp")
    private Long id;

    @Column(nullable = false, length = 200)
    private String titre;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal montant = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatutOpportunite statut;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String descriptionComplete;

    @Column(length = 100)
    private String categorie;

    @Column(name = "date_publication", nullable = false)
    private java.time.LocalDateTime datePublication;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_vendeur", nullable = false)
    private Utilisateur clientVendeur;

    @PrePersist
    void prePersist() {
        if (datePublication == null) {
            datePublication = java.time.LocalDateTime.now();
        }
        if (montant == null) {
            montant = java.math.BigDecimal.ZERO;
        }
        if (statut == null) {
            statut = StatutOpportunite.DISPONIBLE;
        }
    }
}
