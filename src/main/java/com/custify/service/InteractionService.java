package com.custify.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.custify.dto.CreerInteractionRequest;
import com.custify.exception.ClientNonTrouveException;
import com.custify.model.Client;
import com.custify.model.Interaction;
import com.custify.model.Utilisateur;
import com.custify.model.enums.Role;
import com.custify.repository.ClientRepository;
import com.custify.repository.InteractionRepository;

@Service
public class InteractionService {

    @Autowired
    private InteractionRepository interactionRepository;

    @Autowired
    private ClientRepository clientRepository;

    /**
     * Crée une nouvelle interaction liée à un client
     */
    @Transactional
    public Interaction createInteraction(CreerInteractionRequest request, Utilisateur utilisateur) {
        // Vérifier que le client existe
        Client client = clientRepository.findById(request.getClientId())
                .orElseThrow(() -> new ClientNonTrouveException(request.getClientId()));

        // Vérifier que l'utilisateur peut créer une interaction pour ce client
        // (Admin peut tous, Commercial peut seulement pour ses clients)
        if (!utilisateur.getRole().equals(Role.ADMIN) && !client.getUtilisateur().getId().equals(utilisateur.getId())) {
            throw new RuntimeException("Vous n'avez pas la permission de créer une interaction pour ce client.");
        }

        // Créer l'interaction
        Interaction interaction = new Interaction();
        interaction.setType(request.getType());
        interaction.setDateHeure(request.getDateHeure());
        interaction.setResume(request.getResume());
        interaction.setClient(client);
        interaction.setUtilisateur(utilisateur);

        return interactionRepository.save(interaction);
    }

    /**
     * Récupère toutes les interactions d'un utilisateur (Admin = tous, Commercial = les siennes)
     */
    public List<Interaction> getInteractionsByUser(Utilisateur utilisateur) {
        if (utilisateur.getRole().equals(Role.ADMIN)) {
            return interactionRepository.findAll();
        } else {
            return interactionRepository.findByUtilisateurId(utilisateur.getId());
        }
    }

    /**
     * Récupère toutes les interactions d'un client
     */
    public List<Interaction> getInteractionsByClient(Long clientId) {
        return interactionRepository.findByClientId(clientId);
    }

    /**
     * Récupère une interaction spécifique
     */
    public Interaction getInteractionById(Long id) {
        return interactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Interaction non trouvée avec l'ID: " + id));
    }

    /**
     * Met à jour une interaction existante
     */
    @Transactional
    public Interaction updateInteraction(Long id, CreerInteractionRequest request, Utilisateur utilisateur) {
        Interaction interaction = getInteractionById(id);

        // Vérifier les permissions
        if (!utilisateur.getRole().equals(Role.ADMIN) && !interaction.getUtilisateur().getId().equals(utilisateur.getId())) {
            throw new RuntimeException("Vous n'avez pas la permission de modifier cette interaction.");
        }

        interaction.setType(request.getType());
        interaction.setDateHeure(request.getDateHeure());
        interaction.setResume(request.getResume());

        return interactionRepository.save(interaction);
    }

    /**
     * Supprime une interaction
     */
    @Transactional
    public void deleteInteraction(Long id, Utilisateur utilisateur) {
        Interaction interaction = getInteractionById(id);

        // Vérifier les permissions
        if (!utilisateur.getRole().equals(Role.ADMIN) && !interaction.getUtilisateur().getId().equals(utilisateur.getId())) {
            throw new RuntimeException("Vous n'avez pas la permission de supprimer cette interaction.");
        }

        interactionRepository.deleteById(id);
    }
}
