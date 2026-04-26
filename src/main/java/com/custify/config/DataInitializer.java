package com.custify.config;

import com.custify.model.Utilisateur;
import com.custify.model.enums.Role;
import com.custify.repository.UtilisateurRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initUsers(UtilisateurRepository utilisateurRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            createUserIfMissing(utilisateurRepository, passwordEncoder, "Admin Custify", "admin@custify.local",
                    "Admin123!", Role.ADMIN);
            createUserIfMissing(utilisateurRepository, passwordEncoder, "Commercial Custify",
                    "commercial@custify.local", "Commercial123!", Role.COMMERCIAL);
        };
    }

    private void createUserIfMissing(UtilisateurRepository utilisateurRepository, PasswordEncoder passwordEncoder,
            String nom, String email, String motDePasse, Role role) {
        if (utilisateurRepository.findByEmail(email).isPresent()) {
            return;
        }

        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setNom(nom);
        utilisateur.setEmail(email);
        utilisateur.setMotDePasse(passwordEncoder.encode(motDePasse));
        utilisateur.setRole(role);
        utilisateurRepository.save(utilisateur);
    }
}
