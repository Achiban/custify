package com.custify.config;

import com.custify.model.Secteur;
import com.custify.model.Utilisateur;
import com.custify.model.enums.Role;
import com.custify.repository.SecteurRepository;
import com.custify.repository.UtilisateurRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initData(UtilisateurRepository utilisateurRepository,
                               SecteurRepository secteurRepository,
                               PasswordEncoder passwordEncoder) {
        return args -> {
            // Secteurs par défaut
            for (String nomSecteur : List.of("Informatique", "BTP", "Marketing", "Formation", "Logistique", "Autre")) {
                if (secteurRepository.findByNom(nomSecteur).isEmpty()) {
                    Secteur s = new Secteur();
                    s.setNom(nomSecteur);
                    secteurRepository.save(s);
                }
            }

            // Utilisateurs par défaut
            createUserIfMissing(utilisateurRepository, passwordEncoder,
                    "Admin", "Custify", "admin@custify.local", "Admin123!", Role.ADMIN, null, null, null);
            createUserIfMissing(utilisateurRepository, passwordEncoder,
                    "Commercial", "Custify", "commercial@custify.local", "Commercial123!", Role.COMMERCIAL, null, null, null);
            createUserIfMissing(utilisateurRepository, passwordEncoder,
                    "Client", "Demo", "client@custify.local", "Client123!", Role.CLIENT, "0600000000", "Demo SAS", "1 rue de la Paix, Paris");
        };
    }

    private void createUserIfMissing(UtilisateurRepository repo, PasswordEncoder encoder,
                                     String nom, String prenom, String email, String motDePasse,
                                     Role role, String telephone, String entreprise, String adresse) {
        if (repo.findByEmail(email).isPresent()) return;

        Utilisateur u = new Utilisateur();
        u.setNom(nom);
        u.setPrenom(prenom);
        u.setEmail(email);
        u.setMotDePasse(encoder.encode(motDePasse));
        u.setRole(role);
        u.setTelephone(telephone);
        u.setEntreprise(entreprise);
        u.setAdresse(adresse);
        if (role == Role.CLIENT) {
            u.setDateInscription(LocalDateTime.now());
        }
        repo.save(u);
    }
}
