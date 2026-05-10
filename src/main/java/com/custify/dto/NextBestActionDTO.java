package com.custify.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO pour la recommandation de prochaine action idéale (Next Best Action)
 * selon le stade du cycle de vente
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NextBestActionDTO {

    /** Titre de l'action recommandée (ex: "Prendre contact") */
    private String titre;

    /** Description détaillée de l'action et son contexte */
    private String description;

    /** Niveau de priorité: HIGH, MEDIUM, LOW */
    private String priorite;

    /** Icône Font Awesome (ex: "fa-phone", "fa-envelope") */
    private String icone;

    /** Type d'action: APPEL, EMAIL, REUNION, RELANCE, QUALIFICATION */
    private String typeAction;

    /** Couleur pour l'affichage visuel (ex: "#FF6B6B") */
    private String couleur;
}

