package com.custify.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class InscriptionRequest {

    @NotBlank(message = "Le nom est obligatoire")
    @Size(max = 150)
    private String nom;

    @NotBlank(message = "Le prénom est obligatoire")
    @Size(max = 100)
    private String prenom;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Le format de l'email est invalide")
    @Size(max = 190)
    private String email;

    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 8, max = 100, message = "Le mot de passe doit contenir entre 8 et 100 caractères")
    private String motDePasse;

    @NotBlank(message = "Le téléphone est obligatoire")
    @Size(max = 30)
    private String telephone;

    @Size(max = 150)
    private String entreprise;

    @Size(max = 255)
    private String adresse;

    @Size(max = 50)
    private String siret;

    private List<Long> secteurIds;
}
