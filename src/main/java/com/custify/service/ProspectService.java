package com.custify.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.custify.exception.ProspectNonTrouveException;
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
}
