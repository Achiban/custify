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

    @OneToMany(mappedBy = "utilisateur")
    private List<Client> clients = new ArrayList<>();

    @OneToMany(mappedBy = "utilisateur")
    private List<Prospect> prospects = new ArrayList<>();

    @OneToMany(mappedBy = "utilisateur")
    private List<Interaction> interactions = new ArrayList<>();

    @OneToMany(mappedBy = "createur")
    private List<Canal> canauxCrees = new ArrayList<>();

    @OneToMany(mappedBy = "expediteur")
    private List<Message> messagesEnvoyes = new ArrayList<>();

    @OneToMany(mappedBy = "destinataire")
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
