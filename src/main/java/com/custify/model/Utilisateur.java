package com.custify.model;

import java.util.ArrayList;
import java.util.List;

import com.custify.model.enums.Role;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "utilisateur")
public class Utilisateur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_user")
    private Long id;

    @Column(nullable = false, length = 150)
    private String nom;

    @Column(nullable = false, unique = true, length = 190)
    private String email;

    @Column(name = "mot_de_passe", nullable = false, length = 255)
    private String motDePasse;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role = Role.COMMERCIAL;

    @Column(length = 100)
    private String prenom;

    @Column(length = 30)
    private String telephone;

    @Column(length = 150)
    private String entreprise;

    @Column(length = 255)
    private String adresse;

    @Column(length = 50)
    private String siret;

    @Column(name = "date_inscription")
    private java.time.LocalDateTime dateInscription;

    @jakarta.persistence.ManyToMany
    @jakarta.persistence.JoinTable(
        name = "utilisateur_secteur",
        joinColumns = @jakarta.persistence.JoinColumn(name = "id_user"),
        inverseJoinColumns = @jakarta.persistence.JoinColumn(name = "id_secteur")
    )
    private List<Secteur> secteurs = new ArrayList<>();

    @OneToMany(mappedBy = "clientVendeur", cascade = jakarta.persistence.CascadeType.ALL, orphanRemoval = true)
    private List<Opportunite> opportunitesVendeur = new ArrayList<>();

    @OneToMany(mappedBy = "clientDemandeur", cascade = jakarta.persistence.CascadeType.ALL, orphanRemoval = true)
    private List<DemandeOpportunite> demandesOpportunite = new ArrayList<>();

    @OneToMany(mappedBy = "clientBeneficiaire", cascade = jakarta.persistence.CascadeType.ALL, orphanRemoval = true)
    private List<Affectation> affectationsRecues = new ArrayList<>();

    @OneToMany(mappedBy = "commercial", cascade = jakarta.persistence.CascadeType.ALL, orphanRemoval = true)
    private List<Affectation> affectationsDonnees = new ArrayList<>();

    @OneToMany(mappedBy = "createur", cascade = jakarta.persistence.CascadeType.ALL, orphanRemoval = true)
    private List<Canal> canauxCrees = new ArrayList<>();

    @OneToMany(mappedBy = "expediteur", cascade = jakarta.persistence.CascadeType.ALL, orphanRemoval = true)
    private List<Message> messagesEnvoyes = new ArrayList<>();

    @OneToMany(mappedBy = "destinataire", cascade = jakarta.persistence.CascadeType.ALL, orphanRemoval = true)
    private List<Message> messagesRecus = new ArrayList<>();

    public void setNom(String nom) {
        this.nom = nom;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setMotDePasse(String motDePasse) {
        this.motDePasse = motDePasse;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}
