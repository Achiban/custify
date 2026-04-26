package com.custify.model;

import com.custify.model.enums.StatutProspect;
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
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "prospect")
public class Prospect {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_prospect")
    private Long id;

    @Column(nullable = false, length = 150)
    private String nom;

    @Column(length = 190)
    private String email;

    @Column(length = 100)
    private String source;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatutProspect statut;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_user", nullable = false)
    private Utilisateur utilisateur;
}
