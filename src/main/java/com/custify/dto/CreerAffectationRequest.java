package com.custify.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreerAffectationRequest {

    @NotNull(message = "Le client bénéficiaire est obligatoire")
    private Long clientBeneficiaireId;

    @NotNull(message = "L'opportunité est obligatoire")
    private Long opportuniteId;

    @Size(max = 1000)
    private String messageCommercial;
}
