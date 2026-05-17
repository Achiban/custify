package com.custify.config;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.custify.model.Secteur;
import com.custify.model.Utilisateur;
import com.custify.repository.SecteurRepository;
import com.custify.repository.UtilisateurRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;

class DataInitializerTest {

    @Test
    void initDataShouldCreateSectorsAndUsers() throws Exception {
        UtilisateurRepository utilisateurRepository = mock(UtilisateurRepository.class);
        SecteurRepository secteurRepository = mock(SecteurRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);

        when(utilisateurRepository.findByEmail(any())).thenReturn(Optional.empty());
        when(secteurRepository.findByNom(any())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(any())).thenReturn("encoded");

        DataInitializer initializer = new DataInitializer();
        CommandLineRunner runner = initializer.initData(utilisateurRepository, secteurRepository, passwordEncoder);
        runner.run();

        verify(secteurRepository, atLeastOnce()).save(any(Secteur.class));
        verify(utilisateurRepository, atLeastOnce()).save(any(Utilisateur.class));
    }
}
