package com.custify.service;

import com.custify.model.Affectation;
import com.custify.model.Opportunite;
import com.custify.model.Reunion;
import com.custify.model.Utilisateur;
import com.custify.repository.AffectationRepository;
import com.custify.repository.ReunionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReunionService {

    private final ReunionRepository reunionRepository;
    private final AffectationRepository affectationRepository;

    public ReunionService(ReunionRepository reunionRepository, AffectationRepository affectationRepository) {
        this.reunionRepository = reunionRepository;
        this.affectationRepository = affectationRepository;
    }

    @Transactional
    public Reunion organiserReunion(Long affectationId, LocalDateTime dateReunion, String sujet, String lieu, String description, Utilisateur commercial) {
        Affectation affectation = affectationRepository.findById(affectationId)
                .orElseThrow(() -> new RuntimeException("Affectation non trouvée: " + affectationId));

        Opportunite opp = affectation.getOpportunite();
        Utilisateur acheteur = affectation.getClientBeneficiaire();
        Utilisateur vendeur = opp.getClientVendeur();

        Reunion reunion = new Reunion();
        reunion.setCommercial(commercial);
        reunion.setClientVendeur(vendeur);
        reunion.setClientAcheteur(acheteur);
        reunion.setOpportunite(opp);
        reunion.setDateReunion(dateReunion);
        reunion.setSujet(sujet);
        reunion.setLieu(lieu);
        reunion.setDescription(description);

        return reunionRepository.save(reunion);
    }

    @Transactional(readOnly = true)
    public List<Reunion> listerParCommercial(Utilisateur commercial) {
        return reunionRepository.findByCommercial(commercial);
    }

    @Transactional(readOnly = true)
    public List<Reunion> listerParClient(Utilisateur client) {
        return reunionRepository.findByClientVendeurOrClientAcheteur(client, client);
    }
}
