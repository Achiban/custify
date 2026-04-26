package com.custify.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ModifierUtilisateurRequest {

    @NotBlank(message = "Le nom est obligatoire")
    @Size(max = 150, message = "Le nom ne doit pas depasser 150 caracteres")
    private String nom;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Le format de l'email est invalide")
    @Size(max = 190)
    private String email;
}
