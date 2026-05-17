package com.custify.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CreerOpportuniteMarketplaceRequest {

    @NotBlank(message = "Le titre est obligatoire")
    @Size(max = 200)
    private String titre;

    @NotBlank(message = "La description est obligatoire")
    private String descriptionComplete;

    @NotNull(message = "Le montant est obligatoire")
    private BigDecimal montant;

    @Size(max = 100)
    private String categorie;
}
