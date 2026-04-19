package com.custify.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.custify.model.Utilisateur;
import com.custify.repository.UtilisateurRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;

class DataInitializerTest {

    @Test
    void initUsersShouldCreateOnlyMissingUsers() throws Exception {
        UtilisateurRepository utilisateurRepository = mock(UtilisateurRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);

        when(utilisateurRepository.findByEmail("admin@custify.local"))
                .thenReturn(Optional.of(new Utilisateur()));
        when(utilisateurRepository.findByEmail("commercial@custify.local"))
                .thenReturn(Optional.empty());
        when(passwordEncoder.encode("Commercial123!")).thenReturn("encoded-commercial");

        DataInitializer initializer = new DataInitializer();
        CommandLineRunner runner = initializer.initUsers(utilisateurRepository, passwordEncoder);
        runner.run();

        verify(utilisateurRepository).save(any(Utilisateur.class));
        verify(passwordEncoder).encode("Commercial123!");
    }
}
