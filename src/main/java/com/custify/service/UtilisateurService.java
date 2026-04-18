package com.custify.service;

import com.custify.dto.CreerUtilisateurRequest;
import com.custify.dto.UtilisateurResponse;
import com.custify.exception.EmailDejaUtiliseException;
import com.custify.model.Utilisateur;
import com.custify.repository.UtilisateurRepository;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UtilisateurService {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;

    public UtilisateurService(UtilisateurRepository utilisateurRepository, PasswordEncoder passwordEncoder) {
        this.utilisateurRepository = utilisateurRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UtilisateurResponse creer(CreerUtilisateurRequest requete) {
        String email = requete.getEmail().trim().toLowerCase();

        if (utilisateurRepository.existsByEmail(email)) {
            throw new EmailDejaUtiliseException(email);
        }

        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setNom(requete.getNom().trim());
        utilisateur.setEmail(email);
        utilisateur.setMotDePasse(passwordEncoder.encode(requete.getMotDePasse()));
        utilisateur.setRole(requete.getRole());

        return UtilisateurResponse.from(utilisateurRepository.save(utilisateur));
    }

    @Transactional(readOnly = true)
    public List<UtilisateurResponse> listerTous() {
        return utilisateurRepository.findAll(Sort.by(Sort.Direction.ASC, "nom")).stream()
                .map(UtilisateurResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public UtilisateurResponse trouverParId(Long id) {
        Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'ID: " + id));
        return UtilisateurResponse.from(utilisateur);
    }

    @Transactional
    public UtilisateurResponse modifierRole(Long id, com.custify.model.enums.Role nouveauRole) {
        Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'ID: " + id));

        utilisateur.setRole(nouveauRole);
        return UtilisateurResponse.from(utilisateurRepository.save(utilisateur));
    }
}
