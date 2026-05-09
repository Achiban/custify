package com.custify.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.custify.exception.ProspectNonTrouveException;
import com.custify.model.Client;
import com.custify.model.Prospect;
import com.custify.model.Utilisateur;
import com.custify.model.enums.Role;
import com.custify.model.enums.StatutProspect;
import com.custify.repository.ClientRepository;
import com.custify.repository.ProspectRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProspectServiceTest {

    @Mock
    private ProspectRepository prospectRepository;

    @Mock
    private ClientRepository clientRepository;

    @InjectMocks
    private ProspectService prospectService;

    private Utilisateur commercial;
    private Prospect prospect;

    @BeforeEach
    void setUp() {
        commercial = new Utilisateur();
        commercial.setId(1L);
        commercial.setNom("Jean Commercial");
        commercial.setEmail("jean@mail.com");
        commercial.setMotDePasse("secret");
        commercial.setRole(Role.COMMERCIAL);

        prospect = new Prospect();
        prospect.setId(10L);
        prospect.setNom("Marie Prospect");
        prospect.setEmail("marie@example.com");
        prospect.setSource("Site web");
        prospect.setStatut(StatutProspect.CONTACTE);
        prospect.setUtilisateur(commercial);
    }

    @Test
    void saveProspectShouldValidateAndPersist() {
        when(prospectRepository.existsByEmail("marie@example.com")).thenReturn(false);
        when(clientRepository.existsByEmail("marie@example.com")).thenReturn(false);
        when(prospectRepository.save(any(Prospect.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Prospect resultat = prospectService.saveProspect(prospect, commercial);

        assertNotNull(resultat);
        verify(prospectRepository).save(prospect);
    }

    @Test
    void saveProspectShouldThrowWhenEmailAlreadyExistsInProspects() {
        when(prospectRepository.existsByEmail("marie@example.com")).thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> prospectService.saveProspect(prospect, commercial));

        assertEquals("Un autre prospect utilise deja cet email.", exception.getMessage());
        verify(prospectRepository, never()).save(any(Prospect.class));
    }

    @Test
    void saveProspectShouldThrowWhenEmailAlreadyExistsInClients() {
        when(prospectRepository.existsByEmail("marie@example.com")).thenReturn(false);
        when(clientRepository.existsByEmail("marie@example.com")).thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> prospectService.saveProspect(prospect, commercial));

        assertEquals("Un client avec cet email existe deja.", exception.getMessage());
        verify(prospectRepository, never()).save(any(Prospect.class));
    }

    @Test
    void getProspectByIdShouldThrowWhenNotFound() {
        when(prospectRepository.findById(99L)).thenReturn(Optional.empty());

        ProspectNonTrouveException exception = assertThrows(
                ProspectNonTrouveException.class,
                () -> prospectService.getProspectById(99L));

        assertTrue(exception.getMessage().contains("99"));
    }

    // ===== TESTS POUR LA CONVERSION PROSPECT -> CLIENT =====

    @Test
    void convertProspectToClientShouldCreateClientAndUpdateProspectStatus() {
        // Given
        when(prospectRepository.findById(10L)).thenReturn(Optional.of(prospect));
        when(clientRepository.existsByEmail("marie@example.com")).thenReturn(false);
        when(clientRepository.save(any(Client.class))).thenAnswer(invocation -> {
            Client client = invocation.getArgument(0);
            client.setId(100L);
            return client;
        });
        when(prospectRepository.save(any(Prospect.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Client client = prospectService.convertProspectToClient(10L, commercial);

        // Then
        assertNotNull(client);
        assertEquals("Marie Prospect", client.getNom());
        assertEquals("marie@example.com", client.getEmail());
        assertEquals("INCONNU-10", client.getTelephone());
        assertEquals(commercial, client.getUtilisateur());

        // Verify prospect status was updated to CONVERTI
        assertEquals(StatutProspect.CONVERTI, prospect.getStatut());
        verify(clientRepository).save(any(Client.class));
        verify(prospectRepository).save(prospect);
    }

    @Test
    void convertProspectToClientShouldThrowWhenProspectNotFound() {
        // Given
        when(prospectRepository.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        ProspectNonTrouveException exception = assertThrows(
                ProspectNonTrouveException.class,
                () -> prospectService.convertProspectToClient(99L, commercial));

        assertTrue(exception.getMessage().contains("99"));
        verify(clientRepository, never()).save(any(Client.class));
    }

    @Test
    void convertProspectToClientShouldThrowWhenProspectAlreadyConverted() {
        // Given
        prospect.setStatut(StatutProspect.CONVERTI);
        when(prospectRepository.findById(10L)).thenReturn(Optional.of(prospect));

        // When / Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> prospectService.convertProspectToClient(10L, commercial));

        assertEquals("Ce prospect a deja ete converti en client.", exception.getMessage());
        verify(clientRepository, never()).save(any(Client.class));
    }

    @Test
    void convertProspectToClientShouldThrowWhenClientWithSameEmailExists() {
        // Given
        when(prospectRepository.findById(10L)).thenReturn(Optional.of(prospect));
        when(clientRepository.existsByEmail("marie@example.com")).thenReturn(true);

        // When / Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> prospectService.convertProspectToClient(10L, commercial));

        assertEquals("Un client avec cet email existe deja.", exception.getMessage());
        verify(clientRepository, never()).save(any(Client.class));
    }

    @Test
    void convertProspectToClientShouldPreserveProspectNameAndEmail() {
        // Given
        when(prospectRepository.findById(10L)).thenReturn(Optional.of(prospect));
        when(clientRepository.existsByEmail("marie@example.com")).thenReturn(false);
        when(clientRepository.save(any(Client.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(prospectRepository.save(any(Prospect.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Client client = prospectService.convertProspectToClient(10L, commercial);

        // Then
        ArgumentCaptor<Client> clientCaptor = ArgumentCaptor.forClass(Client.class);
        verify(clientRepository).save(clientCaptor.capture());
        Client savedClient = clientCaptor.getValue();

        assertEquals(prospect.getNom(), savedClient.getNom());
        assertEquals(prospect.getEmail(), savedClient.getEmail());
    }
}