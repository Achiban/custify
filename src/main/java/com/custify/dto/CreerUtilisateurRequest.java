package com.custify.dto;

import com.custify.model.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreerUtilisateurRequest {

    @NotBlank(message = "Le nom est obligatoire")
    @Size(max = 150, message = "Le nom ne doit pas depasser 150 caracteres")
    private String nom;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Le format de l'email est invalide")
    @Size(max = 190)
    private String email;

    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 8, max = 100, message = "Le mot de passe doit contenir entre 8 et 100 caracteres")
    private String motDePasse;

    @NotNull(message = "Le role est obligatoire")
    private Role role;
}
