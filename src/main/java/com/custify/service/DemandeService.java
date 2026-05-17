package com.custify.service;

import com.custify.model.DemandeOpportunite;
import com.custify.model.Opportunite;
import com.custify.model.Utilisateur;
import com.custify.model.enums.StatutDemande;
import com.custify.model.enums.StatutOpportunite;
import com.custify.repository.DemandeOpportuniteRepository;
import com.custify.repository.OpportuniteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DemandeService {

    private final DemandeOpportuniteRepository demandeRepository;
    private final OpportuniteRepository opportuniteRepository;

    public DemandeService(DemandeOpportuniteRepository demandeRepository,
                          OpportuniteRepository opportuniteRepository) {
        this.demandeRepository = demandeRepository;
        this.opportuniteRepository = opportuniteRepository;
    }

    @Transactional
    public DemandeOpportunite creerDemande(Long opportuniteId, Utilisateur client) {
        Opportunite opp = opportuniteRepository.findById(opportuniteId)
                .orElseThrow(() -> new RuntimeException("Opportunité non trouvée: " + opportuniteId));

        if (opp.getStatut() != StatutOpportunite.DISPONIBLE) {
            throw new IllegalStateException("Cette opportunité n'est plus disponible");
        }
        if (opp.getClientVendeur().getId().equals(client.getId())) {
            throw new IllegalStateException("Vous ne pouvez pas demander votre propre opportunité");
        }
        if (demandeRepository.existsByClientDemandeurAndOpportunite(client, opp)) {
            throw new IllegalStateException("Vous avez déjà fait une demande pour cette opportunité");
        }

        DemandeOpportunite demande = new DemandeOpportunite();
        demande.setClientDemandeur(client);
        demande.setOpportunite(opp);
        demande.setStatut(StatutDemande.EN_ATTENTE);
        return demandeRepository.save(demande);
    }

    @Transactional(readOnly = true)
    public List<DemandeOpportunite> listerParClient(Utilisateur client) {
        return demandeRepository.findByClientDemandeur(client);
    }

    @Transactional(readOnly = true)
    public List<DemandeOpportunite> listerEnAttente() {
        return demandeRepository.findByStatut(StatutDemande.EN_ATTENTE);
    }

    @Transactional(readOnly = true)
    public List<DemandeOpportunite> listerToutes() {
        return demandeRepository.findAll();
    }
}
