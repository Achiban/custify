package com.custify.service;

import com.custify.exception.AccesNonAutoriseException;
import com.custify.exception.ClientNonTrouveException;
import com.custify.exception.OpportuniteNonTrouveException;
import com.custify.model.Client;
import com.custify.model.Opportunite;
import com.custify.model.Utilisateur;
import com.custify.model.enums.Role;
import com.custify.model.enums.StatutOpportunite;
import com.custify.repository.ClientRepository;
import com.custify.repository.OpportuniteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class OpportuniteService {

    @Autowired
    private OpportuniteRepository opportuniteRepository;

    @Autowired
    private ClientRepository clientRepository;

    // CREATE
    @Transactional
    public Opportunite saveOpportunite(String titre, BigDecimal montant, StatutOpportunite statut, Long clientId, Utilisateur user) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ClientNonTrouveException(clientId));

        // Verification que l'utilisateur est proprietaire du client (sauf admin)
        if (user.getRole() != Role.ADMIN && !client.getUtilisateur().getId().equals(user.getId())) {
            throw new AccesNonAutoriseException();
        }

        Opportunite opportunite = new Opportunite();
        opportunite.setTitre(titre);
        opportunite.setMontant(montant != null ? montant : BigDecimal.ZERO);
        opportunite.setStatut(statut != null ? statut : StatutOpportunite.OUVERTE);
        opportunite.setClient(client);

        return opportuniteRepository.save(opportunite);
    }

    // LIST by user
    public List<Opportunite> getOpportunitesByUser(Utilisateur user) {
        if (user.getRole() == Role.ADMIN) {
            return opportuniteRepository.findAll();
        }
        return opportuniteRepository.findByClientUtilisateurId(user.getId());
    }

    // GET ONE
    public Opportunite getOpportuniteById(Long id) {
        return opportuniteRepository.findById(id)
                .orElseThrow(() -> new OpportuniteNonTrouveException(id));
    }

    // GET ONE avec controle de propriete (Admin acces total, Commercial limite a ses opportunites)
    public Opportunite getOpportuniteForUser(Long id, Utilisateur user) {
        Opportunite opportunite = getOpportuniteById(id);
        verifierProprietaire(opportunite, user);
        return opportunite;
    }

    // UPDATE
    @Transactional
    public Opportunite updateOpportunite(Long id, String titre, BigDecimal montant, StatutOpportunite statut, Utilisateur user) {
        Opportunite opportunite = getOpportuniteForUser(id, user);

        if (titre != null) {
            opportunite.setTitre(titre);
        }
        if (montant != null) {
            opportunite.setMontant(montant);
        }
        if (statut != null) {
            opportunite.setStatut(statut);
        }

        return opportuniteRepository.save(opportunite);
    }

    // DELETE
    @Transactional
    public void deleteOpportunite(Long id, Utilisateur user) {
        Opportunite opportunite = getOpportuniteForUser(id, user);
        opportuniteRepository.delete(opportunite);
    }

    // GET by status
    public List<Opportunite> getOpportunitesByStatut(Utilisateur user, StatutOpportunite statut) {
        if (user.getRole() == Role.ADMIN) {
            return opportuniteRepository.findByStatut(statut);
        }
        return opportuniteRepository.findByClientUtilisateurIdAndStatut(user.getId(), statut);
    }

    // GET total montant by user
    public BigDecimal getTotalMontantByUser(Utilisateur user) {
        if (user.getRole() == Role.ADMIN) {
            return opportuniteRepository.sumMontantByStatut(StatutOpportunite.GAGNEE);
        }
        return opportuniteRepository.sumMontantByClientUtilisateurIdAndStatut(user.getId(), StatutOpportunite.GAGNEE);
    }

    // GET pipeline (all open opportunities)
    public List<Opportunite> getPipelineByUser(Utilisateur user) {
        if (user.getRole() == Role.ADMIN) {
            return opportuniteRepository.findByStatutIn(
                    List.of(StatutOpportunite.OUVERTE, StatutOpportunite.EN_COURS));
        }
        return opportuniteRepository.findByClientUtilisateurIdAndStatutIn(
                user.getId(), List.of(StatutOpportunite.OUVERTE, StatutOpportunite.EN_COURS));
    }

    private void verifierProprietaire(Opportunite opportunite, Utilisateur user) {
        if (user.getRole() == Role.ADMIN) {
            return;
        }
        if (!opportunite.getClient().getUtilisateur().getId().equals(user.getId())) {
            throw new AccesNonAutoriseException();
        }
    }
}