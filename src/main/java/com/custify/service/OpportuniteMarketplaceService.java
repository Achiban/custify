package com.custify.service;

import com.custify.dto.CreerOpportuniteMarketplaceRequest;
import com.custify.exception.AccesNonAutoriseException;
import com.custify.model.Opportunite;
import com.custify.model.Utilisateur;
import com.custify.model.enums.StatutOpportunite;
import com.custify.repository.OpportuniteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OpportuniteMarketplaceService {

    private final OpportuniteRepository opportuniteRepository;

    public OpportuniteMarketplaceService(OpportuniteRepository opportuniteRepository) {
        this.opportuniteRepository = opportuniteRepository;
    }

    @Transactional
    public Opportunite publier(CreerOpportuniteMarketplaceRequest request, Utilisateur vendeur) {
        Opportunite opp = new Opportunite();
        opp.setTitre(request.getTitre());
        opp.setDescriptionComplete(request.getDescriptionComplete());
        opp.setMontant(request.getMontant());
        opp.setCategorie(request.getCategorie());
        opp.setClientVendeur(vendeur);
        opp.setStatut(StatutOpportunite.DISPONIBLE);
        return opportuniteRepository.save(opp);
    }

    @Transactional(readOnly = true)
    public List<Opportunite> listerDisponibles() {
        return opportuniteRepository.findByStatut(StatutOpportunite.DISPONIBLE);
    }

    @Transactional(readOnly = true)
    public List<Opportunite> listerParVendeur(Utilisateur vendeur) {
        return opportuniteRepository.findByClientVendeur(vendeur);
    }

    @Transactional(readOnly = true)
    public List<Opportunite> listerToutes() {
        return opportuniteRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Opportunite trouverParId(Long id) {
        return opportuniteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Opportunité non trouvée: " + id));
    }

    @Transactional
    public Opportunite modifier(Long id, CreerOpportuniteMarketplaceRequest request, Utilisateur vendeur) {
        Opportunite opp = trouverParId(id);
        if (!opp.getClientVendeur().getId().equals(vendeur.getId())) {
            throw new AccesNonAutoriseException("Vous ne pouvez modifier que vos propres opportunités");
        }
        if (opp.getStatut() != StatutOpportunite.DISPONIBLE) {
            throw new IllegalStateException("Seules les opportunités DISPONIBLE peuvent être modifiées");
        }
        opp.setTitre(request.getTitre());
        opp.setDescriptionComplete(request.getDescriptionComplete());
        opp.setMontant(request.getMontant());
        opp.setCategorie(request.getCategorie());
        return opportuniteRepository.save(opp);
    }

    @Transactional
    public void supprimer(Long id, Utilisateur vendeur) {
        Opportunite opp = trouverParId(id);
        if (!opp.getClientVendeur().getId().equals(vendeur.getId())) {
            throw new AccesNonAutoriseException("Vous ne pouvez supprimer que vos propres opportunités");
        }
        if (opp.getStatut() != StatutOpportunite.DISPONIBLE) {
            throw new IllegalStateException("Seules les opportunités DISPONIBLE peuvent être supprimées");
        }
        opportuniteRepository.delete(opp);
    }
}
