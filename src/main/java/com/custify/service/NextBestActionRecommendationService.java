package com.custify.service;

import com.custify.dto.NextBestActionDTO;
import com.custify.model.Client;
import com.custify.model.Interaction;
import com.custify.model.Opportunite;
import com.custify.model.Prospect;
import com.custify.model.enums.StatutOpportunite;
import com.custify.model.enums.StatutProspect;
import com.custify.repository.InteractionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Service de recommandation de prochaine action idéale (Next Best Action)
 * selon le stade du cycle de vente
 */
@Service
public class NextBestActionRecommendationService {

    @Autowired
    private InteractionRepository interactionRepository;

    /**
     * Recommande la prochaine action idéale pour une opportunité selon son stade
     * et l'historique des interactions
     */
    public NextBestActionDTO getNextBestActionForOpportunite(Opportunite opportunite) {
        if (opportunite == null || opportunite.getStatut() == null) {
            return getDefaultAction();
        }

        StatutOpportunite statut = opportunite.getStatut();

        // Récupérer la dernière interaction pour affiner la recommandation
        long daysSinceLastInteraction = getDaysSinceLastInteractionForClient(opportunite.getClient().getId());

        return switch (statut) {
            case OUVERTE -> getActionOuverte(daysSinceLastInteraction);
            case EN_COURS -> getActionEnCours(daysSinceLastInteraction);
            case GAGNEE -> getActionGagnee();
            case PERDUE -> getActionPerdue();
        };
    }

    /**
     * Recommande la prochaine action idéale pour un prospect selon son stade
     */
    public NextBestActionDTO getNextBestActionForProspect(Prospect prospect) {
        if (prospect == null || prospect.getStatut() == null) {
            return getDefaultAction();
        }

        StatutProspect statut = prospect.getStatut();

        return switch (statut) {
            case NOUVEAU -> getActionNouveauProspect();
            case CONTACTE -> getActionContacteProspect();
            case QUALIFIE -> getActionQualifieProspect();
            case CONVERTI -> getActionConversionProspect();
            case PERDU -> getActionPerduProspect();
        };
    }

    // === Actions pour Opportunités ===

    private NextBestActionDTO getActionOuverte(long daysSinceLastInteraction) {
        if (daysSinceLastInteraction < 0) {
            // Jamais contacté depuis l'ouverture de l'opportunité
            return new NextBestActionDTO(
                    "Qualifier l'opportunite",
                    "Organisez une reunion de decouverte pour qualifier les besoins et confirmer le potentiel commercial.",
                    "HIGH",
                    "fa-users",
                    "REUNION",
                    "#FFA500"  // Orange
            );
        }
        if (daysSinceLastInteraction >= 7) {
            return new NextBestActionDTO(
                    "Relancer le prospect",
                    "Aucun contact depuis plus d'une semaine. Appelez ou envoyez un email de relance pour relancer la discussion.",
                    "HIGH",
                    "fa-phone",
                    "APPEL",
                    "#FF6B6B"  // Rouge
            );
        }
        return new NextBestActionDTO(
                "Qualifier l'opportunite",
                "Organisez une reunion de decouverte pour qualifier les besoins et confirmer le potentiel commercial.",
                "HIGH",
                "fa-users",
                "REUNION",
                "#FFA500"  // Orange
        );
    }

    private NextBestActionDTO getActionEnCours(long daysSinceLastInteraction) {
        if (daysSinceLastInteraction < 0) {
            // Jamais contacté - besoin de préparer proposition
            return new NextBestActionDTO(
                    "Preparer la proposition",
                    "Preparez et envoyez une proposition commerciale adaptee aux besoins identifies.",
                    "MEDIUM",
                    "fa-file-contract",
                    "EMAIL",
                    "#4fc3f7"  // Bleu
            );
        }
        if (daysSinceLastInteraction >= 14) {
            return new NextBestActionDTO(
                    "Relance immediate",
                    "Plus de 2 semaines sans contact. Appelez immediatement pour vous assurer que vous n'avez pas ete oublie.",
                    "HIGH",
                    "fa-phone-alt",
                    "APPEL",
                    "#FF4444"  // Rouge foncé
            );
        }
        if (daysSinceLastInteraction >= 7) {
            return new NextBestActionDTO(
                    "Relancer par email",
                    "Une semaine sans nouvelles. Envoyez un email de suivi avec proposition concrete.",
                    "MEDIUM",
                    "fa-envelope",
                    "EMAIL",
                    "#FFA500"  // Orange
            );
        }
        return new NextBestActionDTO(
                "Preparer la proposition",
                "Preparez et envoyez une proposition commerciale adaptee aux besoins identifies.",
                "MEDIUM",
                "fa-file-contract",
                "EMAIL",
                "#4fc3f7"  // Bleu
        );
    }

    private NextBestActionDTO getActionGagnee() {
        return new NextBestActionDTO(
                "Féliciter & Fidéliser",
                "Contactez le client pour confirmer la date de démarrage et discuter des prochaines étapes de collaboration.",
                "MEDIUM",
                "fa-handshake",
                "REUNION",
                "#4CAF50"  // Vert
        );
    }

    private NextBestActionDTO getActionPerdue() {
        return new NextBestActionDTO(
                "Analyser & Rebondir",
                "Organisez un débrief pour comprendre l'échec et identifier les opportunités futures avec ce prospect.",
                "LOW",
                "fa-chart-line",
                "REUNION",
                "#999"  // Gris
        );
    }

    // === Actions pour Prospects ===

    private NextBestActionDTO getActionNouveauProspect() {
        return new NextBestActionDTO(
                "Prise de contact initiale",
                "Appelez le prospect dès que possible pour vous présenter et qualifier son intérêt.",
                "HIGH",
                "fa-phone",
                "APPEL",
                "#FF6B6B"  // Rouge
        );
    }

    private NextBestActionDTO getActionContacteProspect() {
        return new NextBestActionDTO(
                "Qualification approfondie",
                "Organisez un appel pour bien qualifier les besoins et le budget du prospect.",
                "HIGH",
                "fa-users",
                "REUNION",
                "#FFA500"  // Orange
        );
    }

    private NextBestActionDTO getActionQualifieProspect() {
        return new NextBestActionDTO(
                "Créer une opportunité",
                "Le prospect est qualifié. Créez une opportunité dans le pipeline commercial.",
                "HIGH",
                "fa-star",
                "QUALIFICATION",
                "#4fc3f7"  // Bleu
        );
    }

    private NextBestActionDTO getActionConversionProspect() {
        return new NextBestActionDTO(
                "Convertir en client",
                "Le prospect a validé la proposition. Finalisez la conversion et préparez la signature.",
                "HIGH",
                "fa-check-circle",
                "RELANCE",
                "#4CAF50"  // Vert
        );
    }

    private NextBestActionDTO getActionPerduProspect() {
        return new NextBestActionDTO(
                "Archiver & Monitorer",
                "Le prospect a refusé. Archivez mais gardez un suivi périodique (changement de contexte).",
                "LOW",
                "fa-archive",
                "RELANCE",
                "#999"  // Gris
        );
    }

    // === Utilitaires ===

    /**
     * Calcule le nombre de jours depuis la dernière interaction avec un client
     * Retourne -1 si aucune interaction trouvée
     */
    private long getDaysSinceLastInteractionForClient(Long clientId) {
        if (clientId == null) {
            return -1;
        }

        try {
            List<Interaction> interactions = interactionRepository.findByClientId(clientId);
            Optional<Interaction> lastInteraction = interactions.stream()
                    .max(Comparator.comparing(Interaction::getDateHeure));

            if (lastInteraction.isPresent()) {
                return ChronoUnit.DAYS.between(lastInteraction.get().getDateHeure(), LocalDateTime.now());
            }
        } catch (Exception e) {
            // Si erreur lors de la récupération, retourner une valeur neutre
        }

        return -1; // Aucune interaction trouvée
    }

    /**
     * Retourne l'action par défaut
     */
    private NextBestActionDTO getDefaultAction() {
        return new NextBestActionDTO(
                "Prendre contact",
                "Contactez le prospect par téléphone ou email pour relancer la discussion.",
                "MEDIUM",
                "fa-phone",
                "APPEL",
                "#4fc3f7"  // Bleu
        );
    }
}







