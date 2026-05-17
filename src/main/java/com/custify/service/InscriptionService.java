package com.custify.service;

import com.custify.dto.InscriptionRequest;
import com.custify.exception.EmailDejaUtiliseException;
import com.custify.model.Secteur;
import com.custify.model.Utilisateur;
import com.custify.model.enums.Role;
import com.custify.repository.SecteurRepository;
import com.custify.repository.UtilisateurRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class InscriptionService {

    private final UtilisateurRepository utilisateurRepository;
    private final SecteurRepository secteurRepository;
    private final PasswordEncoder passwordEncoder;

    public InscriptionService(UtilisateurRepository utilisateurRepository,
                               SecteurRepository secteurRepository,
                               PasswordEncoder passwordEncoder) {
        this.utilisateurRepository = utilisateurRepository;
        this.secteurRepository = secteurRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Utilisateur inscrireClient(InscriptionRequest request) {
        String email = request.getEmail().trim().toLowerCase();

        if (utilisateurRepository.existsByEmail(email)) {
            throw new EmailDejaUtiliseException(email);
        }

        Utilisateur client = new Utilisateur();
        client.setNom(request.getNom().trim());
        client.setPrenom(request.getPrenom().trim());
        client.setEmail(email);
        client.setMotDePasse(passwordEncoder.encode(request.getMotDePasse()));
        client.setRole(Role.CLIENT);
        client.setTelephone(request.getTelephone());
        client.setEntreprise(request.getEntreprise());
        client.setAdresse(request.getAdresse());
        client.setSiret(request.getSiret());
        client.setDateInscription(LocalDateTime.now());

        if (request.getSecteurIds() != null && !request.getSecteurIds().isEmpty()) {
            List<Secteur> secteurs = secteurRepository.findAllById(request.getSecteurIds());
            client.setSecteurs(secteurs);
        }

        return utilisateurRepository.save(client);
    }

    @Transactional(readOnly = true)
    public List<Secteur> listerSecteurs() {
        return secteurRepository.findAll();
    }
}
