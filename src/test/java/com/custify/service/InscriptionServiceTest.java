package com.custify.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.custify.dto.InscriptionRequest;
import com.custify.exception.EmailDejaUtiliseException;
import com.custify.model.Secteur;
import com.custify.model.Utilisateur;
import com.custify.model.enums.Role;
import com.custify.repository.SecteurRepository;
import com.custify.repository.UtilisateurRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class InscriptionServiceTest {

    @Mock
    private UtilisateurRepository utilisateurRepository;

    @Mock
    private SecteurRepository secteurRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private InscriptionService inscriptionService;

    @BeforeEach
    void setUp() {
        inscriptionService = new InscriptionService(utilisateurRepository, secteurRepository, passwordEncoder);
    }

    @Test
    void inscrireClientShouldCreateClientWithRoleClient() {
        InscriptionRequest request = buildRequest("  Alice  ", " ALICE@MAIL.COM ", "secret");
        request.setSecteurIds(null);

        when(utilisateurRepository.existsByEmail("alice@mail.com")).thenReturn(false);
        when(passwordEncoder.encode("secret")).thenReturn("encoded-secret");
        when(utilisateurRepository.save(any(Utilisateur.class))).thenAnswer(inv -> {
            Utilisateur u = inv.getArgument(0);
            u.setId(1L);
            return u;
        });

        Utilisateur result = inscriptionService.inscrireClient(request);

        assertEquals(Role.CLIENT, result.getRole());
        assertEquals("Alice", result.getNom());
        assertEquals("alice@mail.com", result.getEmail());
        assertEquals("encoded-secret", result.getMotDePasse());
    }

    @Test
    void inscrireClientShouldThrowWhenEmailAlreadyExists() {
        InscriptionRequest request = buildRequest("Bob", "bob@mail.com", "pass");

        when(utilisateurRepository.existsByEmail("bob@mail.com")).thenReturn(true);

        assertThrows(EmailDejaUtiliseException.class, () -> inscriptionService.inscrireClient(request));
        verify(utilisateurRepository, never()).save(any());
    }

    @Test
    void inscrireClientShouldNormalizeEmailToLowerCase() {
        InscriptionRequest request = buildRequest("Carol", "  CAROL@EXAMPLE.COM  ", "pass");
        request.setSecteurIds(null);

        when(utilisateurRepository.existsByEmail("carol@example.com")).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("hashed");
        when(utilisateurRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Utilisateur result = inscriptionService.inscrireClient(request);

        assertEquals("carol@example.com", result.getEmail());
    }

    @Test
    void inscrireClientShouldAttachSecteurs() {
        InscriptionRequest request = buildRequest("Dave", "dave@mail.com", "pass");
        request.setSecteurIds(List.of(1L, 2L));

        Secteur s1 = new Secteur();
        s1.setId(1L);
        Secteur s2 = new Secteur();
        s2.setId(2L);

        when(utilisateurRepository.existsByEmail("dave@mail.com")).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("hashed");
        when(secteurRepository.findAllById(List.of(1L, 2L))).thenReturn(List.of(s1, s2));
        when(utilisateurRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Utilisateur result = inscriptionService.inscrireClient(request);

        assertEquals(2, result.getSecteurs().size());
    }

    @Test
    void listerSecteursShouldReturnAll() {
        Secteur s = new Secteur();
        s.setId(1L);
        when(secteurRepository.findAll()).thenReturn(List.of(s));

        List<Secteur> result = inscriptionService.listerSecteurs();

        assertEquals(1, result.size());
    }

    private InscriptionRequest buildRequest(String nom, String email, String password) {
        InscriptionRequest req = new InscriptionRequest();
        req.setNom(nom);
        req.setPrenom("Prenom");
        req.setEmail(email);
        req.setMotDePasse(password);
        req.setTelephone("0600000000");
        req.setEntreprise("Entreprise");
        req.setAdresse("1 rue Test");
        req.setSiret("12345678900000");
        return req;
    }
}
