package com.custify.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "client")
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_client")
    private Long id;

    @Column(nullable = false, length = 150)
    private String nom;

    @Column(length = 190)
    private String email;

    @Column(length = 30)
    private String telephone;

    @Column(length = 150)
    private String entreprise;

    @Column(name = "date_creation", nullable = false)
    private LocalDateTime dateCreation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_user", nullable = false)
    private Utilisateur utilisateur;

    @OneToMany(mappedBy = "client")
    private List<Interaction> interactions = new ArrayList<>();

    @OneToMany(mappedBy = "client")
    private List<Opportunite> opportunites = new ArrayList<>();

    @PrePersist
    void prePersist() {
        if (dateCreation == null) {
            dateCreation = LocalDateTime.now();
        }
    }
}
