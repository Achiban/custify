package com.custify.dto;

import com.custify.model.enums.TypeInteraction;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreerInteractionRequest {

    @NotNull(message = "Le type d'interaction est obligatoire")
    private TypeInteraction type;

    @NotNull(message = "La date et heure sont obligatoires")
    private LocalDateTime dateHeure;

    @NotBlank(message = "Le resume de l'interaction est obligatoire")
    @Size(max = 1000, message = "Le resume ne doit pas depasser 1000 caracteres")
    private String resume;

    @NotNull(message = "L'ID du client est obligatoire")
    private Long clientId;

    // Getter et Setter automatiques via Lombok
}
