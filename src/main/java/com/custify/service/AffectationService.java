package com.custify.service;

import com.custify.dto.CreerAffectationRequest;
import com.custify.model.Affectation;
import com.custify.model.Opportunite;
import com.custify.model.Utilisateur;
import com.custify.model.enums.Role;
import com.custify.model.enums.StatutAffectation;
import com.custify.model.enums.StatutOpportunite;
import com.custify.repository.AffectationRepository;
import com.custify.repository.OpportuniteRepository;
import com.custify.repository.UtilisateurRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AffectationService {

    private final AffectationRepository affectationRepository;
    private final OpportuniteRepository opportuniteRepository;
    private final UtilisateurRepository utilisateurRepository;

    public AffectationService(AffectationRepository affectationRepository,
                               OpportuniteRepository opportuniteRepository,
                               UtilisateurRepository utilisateurRepository) {
        this.affectationRepository = affectationRepository;
        this.opportuniteRepository = opportuniteRepository;
        this.utilisateurRepository = utilisateurRepository;
    }

    @Transactional
    public Affectation creerAffectation(CreerAffectationRequest request, Utilisateur commercial) {
        Utilisateur beneficiaire = utilisateurRepository.findById(request.getClientBeneficiaireId())
                .orElseThrow(() -> new RuntimeException("Client non trouvé: " + request.getClientBeneficiaireId()));

        if (beneficiaire.getRole() != Role.CLIENT) {
            throw new IllegalArgumentException("Le bénéficiaire doit avoir le rôle CLIENT");
        }

        Opportunite opp = opportuniteRepository.findById(request.getOpportuniteId())
                .orElseThrow(() -> new RuntimeException("Opportunité non trouvée: " + request.getOpportuniteId()));

        if (opp.getStatut() != StatutOpportunite.DISPONIBLE) {
            throw new IllegalStateException("L'opportunité doit être DISPONIBLE pour être affectée");
        }

        // Passer l'opportunité en ATTRIBUEE
        opp.setStatut(StatutOpportunite.ATTRIBUEE);
        opportuniteRepository.save(opp);

        Affectation affectation = new Affectation();
        affectation.setCommercial(commercial);
        affectation.setClientBeneficiaire(beneficiaire);
        affectation.setOpportunite(opp);
        affectation.setMessageCommercial(request.getMessageCommercial());
        affectation.setStatutClient(StatutAffectation.EN_ATTENTE);
        return affectationRepository.save(affectation);
    }

    @Transactional
    public void accepterAffectation(Long affectationId, Utilisateur client) {
        Affectation affectation = trouverParId(affectationId);
        verifierBeneficiaire(affectation, client);

        affectation.setStatutClient(StatutAffectation.ACCEPTEE);
        affectationRepository.save(affectation);

        // L'opportunité passe CONCLUE
        Opportunite opp = affectation.getOpportunite();
        opp.setStatut(StatutOpportunite.CONCLUE);
        opportuniteRepository.save(opp);
    }

    @Transactional
    public void refuserAffectation(Long affectationId, Utilisateur client) {
        Affectation affectation = trouverParId(affectationId);
        verifierBeneficiaire(affectation, client);

        affectation.setStatutClient(StatutAffectation.REFUSEE);
        affectationRepository.save(affectation);

        // L'opportunité redevient DISPONIBLE
        Opportunite opp = affectation.getOpportunite();
        opp.setStatut(StatutOpportunite.DISPONIBLE);
        opportuniteRepository.save(opp);
    }

    @Transactional(readOnly = true)
    public List<Affectation> listerParClient(Utilisateur client) {
        return affectationRepository.findByClientBeneficiaire(client);
    }

    @Transactional(readOnly = true)
    public List<Affectation> listerParCommercial(Utilisateur commercial) {
        return affectationRepository.findByCommercial(commercial);
    }

    @Transactional(readOnly = true)
    public List<Affectation> listerToutes() {
        return affectationRepository.findAll();
    }

    private Affectation trouverParId(Long id) {
        return affectationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Affectation non trouvée: " + id));
    }

    private void verifierBeneficiaire(Affectation affectation, Utilisateur client) {
        if (!affectation.getClientBeneficiaire().getId().equals(client.getId())) {
            throw new com.custify.exception.AccesNonAutoriseException("Cette affectation ne vous appartient pas");
        }
        if (affectation.getStatutClient() != StatutAffectation.EN_ATTENTE) {
            throw new IllegalStateException("Cette affectation a déjà été traitée");
        }
    }
}
