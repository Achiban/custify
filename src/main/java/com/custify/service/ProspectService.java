package com.custify.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.custify.exception.ProspectNonTrouveException;
import com.custify.model.Client;
import com.custify.model.Prospect;
import com.custify.model.Utilisateur;
import com.custify.model.enums.StatutProspect;
import com.custify.repository.ClientRepository;
import com.custify.repository.ProspectRepository;

@Service
public class ProspectService {

    @Autowired
    private ProspectRepository prospectRepository;

    @Autowired
    private ClientRepository clientRepository;

    public Prospect saveProspect(Prospect prospect, Utilisateur utilisateur) {
        validateProspect(prospect);
        prospect.setNom(prospect.getNom().trim());
        prospect.setEmail(prospect.getEmail().trim());
        prospect.setSource(prospect.getSource().trim());

        if (clientRepository.existsByEmail(prospect.getEmail())) {
            throw new IllegalArgumentException("Un client avec cet email existe deja.");
        }

        if (prospectRepository.existsByEmail(prospect.getEmail())) {
            throw new IllegalArgumentException("Un autre prospect utilise deja cet email.");
        }

        prospect.setUtilisateur(utilisateur);
        return prospectRepository.save(prospect);
    }

    public List<Prospect> getAllProspects() {
        return prospectRepository.findAll();
    }

    public Prospect getProspectById(Long id) {
        return prospectRepository.findById(id)
                .orElseThrow(() -> new ProspectNonTrouveException(id));
    }

    private void validateProspect(Prospect prospect) {
        if (prospect == null) {
            throw new IllegalArgumentException("Prospect invalide.");
        }

        if (!StringUtils.hasText(prospect.getNom())) {
            throw new IllegalArgumentException("Le nom du prospect est obligatoire.");
        }

        if (!StringUtils.hasText(prospect.getEmail())) {
            throw new IllegalArgumentException("L'email du prospect est obligatoire.");
        }

        if (!StringUtils.hasText(prospect.getSource())) {
            throw new IllegalArgumentException("La source du prospect est obligatoire.");
        }

        if (prospect.getStatut() == null) {
            throw new IllegalArgumentException("Le statut du prospect est obligatoire.");
        }

        if (!isValidStatut(prospect.getStatut())) {
            throw new IllegalArgumentException("Le statut du prospect n'est pas valide.");
        }
    }

    private boolean isValidStatut(StatutProspect statut) {
        if (statut == null) {
            return false;
        }
        for (StatutProspect value : StatutProspect.values()) {
            if (value == statut) {
                return true;
            }
        }
        return false;
    }

    /**
     * Convertit un prospect en client en un clic.
     * Cette methode cree un nouveau client a partir des donnees du prospect,
     * met a jour le statut du prospect a CONVERTI, et retourne le client cree.
     *
     * @param prospectId l'identifiant du prospect a convertir
     * @param utilisateur l'utilisateur qui effectue la conversion
     * @return le client cree
     * @throws ProspectNonTrouveException si le prospect n'existe pas
     * @throws IllegalArgumentException si le prospect est deja converti ou si l'email existe deja
     */
    @Transactional
    public Client convertProspectToClient(Long prospectId, Utilisateur utilisateur) {
        // Recuperer le prospect
        Prospect prospect = getProspectById(prospectId);

        // Verifier que le prospect n'est pas deja converti
        if (prospect.getStatut() == StatutProspect.CONVERTI) {
            throw new IllegalArgumentException("Ce prospect a deja ete converti en client.");
        }

        // Verifier qu'un client avec le meme email n'existe pas deja
        if (clientRepository.existsByEmail(prospect.getEmail())) {
            throw new IllegalArgumentException("Un client avec cet email existe deja.");
        }

        // Creer un nouveau client a partir du prospect
        Client client = new Client();
        client.setNom(prospect.getNom());
        client.setEmail(prospect.getEmail());
        // Generer un numero de telephone temporaire car le champ est obligatoire
        // Le commercial pourra le modifier plus tard
        client.setTelephone("INCONNU-" + prospect.getId());
        client.setUtilisateur(utilisateur);
        // Les autres champs (entreprise) restent null et pourront etre completes plus tard

        // Sauvegarder le client
        clientRepository.save(client);

        // Mettre a jour le statut du prospect a CONVERTI
        prospect.setStatut(StatutProspect.CONVERTI);
        prospectRepository.save(prospect);

        return client;
    }
}
