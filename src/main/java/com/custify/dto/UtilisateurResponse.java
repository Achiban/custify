package com.custify.dto;

import com.custify.model.Utilisateur;
import com.custify.model.enums.Role;
import lombok.Getter;

@Getter
public class UtilisateurResponse {

    private final Long id;
    private final String nom;
    private final String email;
    private final Role role;

    public UtilisateurResponse(Long id, String nom, String email, Role role) {
        this.id = id;
        this.nom = nom;
        this.email = email;
        this.role = role;
    }

    public static UtilisateurResponse from(Utilisateur utilisateur) {
        return new UtilisateurResponse(
                utilisateur.getId(),
                utilisateur.getNom(),
                utilisateur.getEmail(),
                utilisateur.getRole());
    }
}
