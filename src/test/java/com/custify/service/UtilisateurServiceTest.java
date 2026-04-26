package com.custify.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.custify.dto.CreerUtilisateurRequest;
import com.custify.dto.UtilisateurResponse;
import com.custify.exception.EmailDejaUtiliseException;
import com.custify.model.Utilisateur;
import com.custify.model.enums.Role;
import com.custify.repository.UtilisateurRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UtilisateurServiceTest {

    @Mock
    private UtilisateurRepository utilisateurRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UtilisateurService utilisateurService;

    @BeforeEach
    void setUp() {
        utilisateurService = new UtilisateurService(utilisateurRepository, passwordEncoder);
    }

    @Test
    void creerShouldNormalizeFieldsEncodePasswordAndReturnResponse() {
        CreerUtilisateurRequest request = new CreerUtilisateurRequest();
        request.setNom("  Alice Dupont  ");
        request.setEmail("  ALICE@MAIL.COM  ");
        request.setMotDePasse("motdepassefort");
        request.setRole(Role.ADMIN);

        when(utilisateurRepository.existsByEmail("alice@mail.com")).thenReturn(false);
        when(passwordEncoder.encode("motdepassefort")).thenReturn("hashed-password");
        when(utilisateurRepository.save(any(Utilisateur.class))).thenAnswer(invocation -> {
            Utilisateur saved = invocation.getArgument(0);
            saved.setId(42L);
            return saved;
        });

        UtilisateurResponse response = utilisateurService.creer(request);

        assertEquals(42L, response.getId());
        assertEquals("Alice Dupont", response.getNom());
        assertEquals("alice@mail.com", response.getEmail());
        assertEquals(Role.ADMIN, response.getRole());

        ArgumentCaptor<Utilisateur> captor = ArgumentCaptor.forClass(Utilisateur.class);
        verify(utilisateurRepository).save(captor.capture());
        Utilisateur utilisateurSauvegarde = captor.getValue();
        assertEquals("Alice Dupont", utilisateurSauvegarde.getNom());
        assertEquals("alice@mail.com", utilisateurSauvegarde.getEmail());
        assertEquals("hashed-password", utilisateurSauvegarde.getMotDePasse());
        assertEquals(Role.ADMIN, utilisateurSauvegarde.getRole());
    }

    @Test
    void creerShouldThrowWhenEmailAlreadyExists() {
        CreerUtilisateurRequest request = new CreerUtilisateurRequest();
        request.setNom("Alice");
        request.setEmail("Alice@mail.com");
        request.setMotDePasse("motdepassefort");
        request.setRole(Role.ADMIN);

        when(utilisateurRepository.existsByEmail("alice@mail.com")).thenReturn(true);

        EmailDejaUtiliseException exception = assertThrows(
                EmailDejaUtiliseException.class,
                () -> utilisateurService.creer(request));

        assertTrue(exception.getMessage().contains("alice@mail.com"));
        verify(passwordEncoder, never()).encode(any());
        verify(utilisateurRepository, never()).save(any(Utilisateur.class));
    }

    @Test
    void listerTousShouldMapEntitiesToResponses() {
        Utilisateur premier = new Utilisateur();
        premier.setId(1L);
        premier.setNom("Alice");
        premier.setEmail("alice@mail.com");
        premier.setRole(Role.ADMIN);

        Utilisateur second = new Utilisateur();
        second.setId(2L);
        second.setNom("Bob");
        second.setEmail("bob@mail.com");
        second.setRole(Role.COMMERCIAL);

        when(utilisateurRepository.findAll(any(Sort.class))).thenReturn(List.of(premier, second));

        List<UtilisateurResponse> resultats = utilisateurService.listerTous();

        assertEquals(2, resultats.size());
        assertEquals("Alice", resultats.get(0).getNom());
        assertEquals(Role.COMMERCIAL, resultats.get(1).getRole());

        ArgumentCaptor<Sort> sortCaptor = ArgumentCaptor.forClass(Sort.class);
        verify(utilisateurRepository).findAll(sortCaptor.capture());
        Sort.Order ordre = sortCaptor.getValue().getOrderFor("nom");
        assertEquals(Sort.Direction.ASC, ordre.getDirection());
    }

    @Test
    void trouverParIdShouldReturnMappedResponseWhenFound() {
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setId(10L);
        utilisateur.setNom("Nadia");
        utilisateur.setEmail("nadia@mail.com");
        utilisateur.setRole(Role.COMMERCIAL);

        when(utilisateurRepository.findById(10L)).thenReturn(Optional.of(utilisateur));

        UtilisateurResponse resultat = utilisateurService.trouverParId(10L);

        assertEquals(10L, resultat.getId());
        assertEquals("Nadia", resultat.getNom());
        assertEquals("nadia@mail.com", resultat.getEmail());
        assertEquals(Role.COMMERCIAL, resultat.getRole());
    }

    @Test
    void trouverParIdShouldThrowWhenUserNotFound() {
        when(utilisateurRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> utilisateurService.trouverParId(99L));

        assertTrue(exception.getMessage().contains("99"));
    }

    @Test
    void modifierRoleShouldUpdateRoleAndSave() {
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setId(7L);
        utilisateur.setNom("Leo");
        utilisateur.setEmail("leo@mail.com");
        utilisateur.setRole(Role.COMMERCIAL);

        when(utilisateurRepository.findById(7L)).thenReturn(Optional.of(utilisateur));
        when(utilisateurRepository.save(any(Utilisateur.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UtilisateurResponse resultat = utilisateurService.modifierRole(7L, Role.ADMIN);

        assertEquals(Role.ADMIN, resultat.getRole());

        ArgumentCaptor<Utilisateur> captor = ArgumentCaptor.forClass(Utilisateur.class);
        verify(utilisateurRepository).save(captor.capture());
        assertEquals(Role.ADMIN, captor.getValue().getRole());
    }

    @Test
    void modifierRoleShouldThrowWhenUserNotFound() {
        when(utilisateurRepository.findById(21L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> utilisateurService.modifierRole(21L, Role.ADMIN));

        assertTrue(exception.getMessage().contains("21"));
    }

    @Test
    void modifierShouldUpdateNameAndEmailWhenAvailable() {
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setId(11L);
        utilisateur.setNom("Ancien Nom");
        utilisateur.setEmail("ancien@mail.com");
        utilisateur.setRole(Role.COMMERCIAL);

        com.custify.dto.ModifierUtilisateurRequest request = new com.custify.dto.ModifierUtilisateurRequest();
        request.setNom("  Nouveau Nom  ");
        request.setEmail("  nouveau@mail.com  ");

        when(utilisateurRepository.findById(11L)).thenReturn(Optional.of(utilisateur));
        when(utilisateurRepository.existsByEmail("nouveau@mail.com")).thenReturn(false);
        when(utilisateurRepository.save(any(Utilisateur.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UtilisateurResponse resultat = utilisateurService.modifier(11L, request);

        assertEquals("Nouveau Nom", resultat.getNom());
        assertEquals("nouveau@mail.com", resultat.getEmail());

        ArgumentCaptor<Utilisateur> captor = ArgumentCaptor.forClass(Utilisateur.class);
        verify(utilisateurRepository).save(captor.capture());
        assertEquals("Nouveau Nom", captor.getValue().getNom());
        assertEquals("nouveau@mail.com", captor.getValue().getEmail());
    }

    @Test
    void modifierShouldThrowWhenEmailAlreadyExists() {
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setId(12L);
        utilisateur.setNom("Ancien Nom");
        utilisateur.setEmail("ancien@mail.com");

        com.custify.dto.ModifierUtilisateurRequest request = new com.custify.dto.ModifierUtilisateurRequest();
        request.setNom("Nouveau Nom");
        request.setEmail("deja@mail.com");

        when(utilisateurRepository.findById(12L)).thenReturn(Optional.of(utilisateur));
        when(utilisateurRepository.existsByEmail("deja@mail.com")).thenReturn(true);

        EmailDejaUtiliseException exception = assertThrows(
                EmailDejaUtiliseException.class,
                () -> utilisateurService.modifier(12L, request));

        assertTrue(exception.getMessage().contains("deja@mail.com"));
        verify(utilisateurRepository, never()).save(any(Utilisateur.class));
    }

    @Test
    void supprimerShouldDeleteExistingUser() {
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setId(13L);
        utilisateur.setNom("Suppression");
        utilisateur.setEmail("delete@mail.com");

        when(utilisateurRepository.findById(13L)).thenReturn(Optional.of(utilisateur));

        utilisateurService.supprimer(13L);

        verify(utilisateurRepository).delete(utilisateur);
    }

    @Test
    void supprimerShouldThrowWhenUserNotFound() {
        when(utilisateurRepository.findById(14L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> utilisateurService.supprimer(14L));

        assertTrue(exception.getMessage().contains("14"));
    }
}
