package com.custify.dto;

import com.custify.model.enums.StatutOpportunite;
import java.math.BigDecimal;

public class CreerOpportuniteRequest {

    private String titre;
    private BigDecimal montant;
    private StatutOpportunite statut;
    private Long clientId;

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public BigDecimal getMontant() {
        return montant;
    }

    public void setMontant(BigDecimal montant) {
        this.montant = montant;
    }

    public StatutOpportunite getStatut() {
        return statut;
    }

    public void setStatut(StatutOpportunite statut) {
        this.statut = statut;
    }

    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }
}