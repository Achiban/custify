package com.custify.model;

import com.custify.model.enums.TypeCanal;
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
@Table(name = "canal")
public class Canal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_canal")
    private Long id;

    @Column(nullable = false, length = 150)
    private String nom;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TypeCanal type;

    @Column(name = "date_creation", nullable = false)
    private LocalDateTime dateCreation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cree_par", nullable = false)
    private Utilisateur createur;

    @OneToMany(mappedBy = "canal")
    private List<Message> messages = new ArrayList<>();

    @OneToMany(mappedBy = "canal")
    private List<ParticipantCanal> participants = new ArrayList<>();

    @PrePersist
    void prePersist() {
        if (dateCreation == null) {
            dateCreation = LocalDateTime.now();
        }
    }
}
