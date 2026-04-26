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

    @Column(name = "motif_cloture", length = 255)
    private String motifCloture;

    @Column(name = "date_creation", nullable = false)
    private LocalDateTime dateCreation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_client", nullable = false)
    private Client client;

    @PrePersist
    void prePersist() {
        if (dateCreation == null) {
            dateCreation = LocalDateTime.now();
        }
        if (montant == null) {
            montant = BigDecimal.ZERO;
        }
    }
}
