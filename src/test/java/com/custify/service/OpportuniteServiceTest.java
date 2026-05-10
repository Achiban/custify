package com.custify.service;

import com.custify.exception.AccesNonAutoriseException;
import com.custify.exception.ClientNonTrouveException;
import com.custify.exception.OpportuniteNonTrouveException;
import com.custify.model.Client;
import com.custify.model.Opportunite;
import com.custify.model.Utilisateur;
import com.custify.model.enums.Role;
import com.custify.model.enums.StatutOpportunite;
import com.custify.repository.ClientRepository;
import com.custify.repository.OpportuniteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OpportuniteServiceTest {

    @Mock
    private OpportuniteRepository opportuniteRepository;

    @Mock
    private ClientRepository clientRepository;

    @InjectMocks
    private OpportuniteService opportuniteService;

    private Utilisateur commercialUser;
    private Utilisateur adminUser;
    private Client client;
    private Client otherClient;
    private Opportunite opportunite;

    @BeforeEach
    void setUp() {
        commercialUser = new Utilisateur();
        commercialUser.setId(1L);
        commercialUser.setRole(Role.COMMERCIAL);

        adminUser = new Utilisateur();
        adminUser.setId(2L);
        adminUser.setRole(Role.ADMIN);

        client = new Client();
        client.setId(10L);
        client.setNom("Client Test");
        client.setUtilisateur(commercialUser);

        otherClient = new Client();
        otherClient.setId(11L);
        otherClient.setNom("Other Client");
        otherClient.setUtilisateur(new Utilisateur());
        otherClient.getUtilisateur().setId(99L);

        opportunite = new Opportunite();
        opportunite.setId(100L);
        opportunite.setTitre("Test Opportunite");
        opportunite.setMontant(BigDecimal.valueOf(1000));
        opportunite.setStatut(StatutOpportunite.OUVERTE);
        opportunite.setClient(client);
    }

    @Test
    void saveOpportunite_Success() {
        when(clientRepository.findById(client.getId())).thenReturn(Optional.of(client));
        when(opportuniteRepository.save(any(Opportunite.class))).thenAnswer(invocation -> {
            Opportunite savedOpp = invocation.getArgument(0);
            savedOpp.setId(200L);
            return savedOpp;
        });

        Opportunite result = opportuniteService.saveOpportunite(
                "Nouvelle Opportunite",
                BigDecimal.valueOf(500),
                StatutOpportunite.EN_COURS,
                client.getId(),
                commercialUser
        );

        assertNotNull(result);
        assertEquals("Nouvelle Opportunite", result.getTitre());
        assertEquals(BigDecimal.valueOf(500), result.getMontant());
        assertEquals(StatutOpportunite.EN_COURS, result.getStatut());
        verify(opportuniteRepository).save(any(Opportunite.class));
    }

    @Test
    void saveOpportunite_ClientNotFound_ThrowsException() {
        when(clientRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ClientNonTrouveException.class, () -> {
            opportuniteService.saveOpportunite(
                    "Test",
                    BigDecimal.ZERO,
                    StatutOpportunite.OUVERTE,
                    999L,
                    commercialUser
            );
        });
    }

    @Test
    void saveOpportunite_Unauthorized_ThrowsException() {
        when(clientRepository.findById(otherClient.getId())).thenReturn(Optional.of(otherClient));

        assertThrows(AccesNonAutoriseException.class, () -> {
            opportuniteService.saveOpportunite(
                    "Test",
                    BigDecimal.ZERO,
                    StatutOpportunite.OUVERTE,
                    otherClient.getId(),
                    commercialUser
            );
        });
    }

    @Test
    void saveOpportunite_AdminCanCreateForAnyClient() {
        when(clientRepository.findById(otherClient.getId())).thenReturn(Optional.of(otherClient));
        when(opportuniteRepository.save(any(Opportunite.class))).thenReturn(opportunite);

        Opportunite result = opportuniteService.saveOpportunite(
                "Admin Opportunite",
                BigDecimal.valueOf(2000),
                StatutOpportunite.OUVERTE,
                otherClient.getId(),
                adminUser
        );

        assertNotNull(result);
        verify(opportuniteRepository).save(any(Opportunite.class));
    }

    @Test
    void getOpportunitesByUser_CommercialGetsOnlyOwnOpportunities() {
        Opportunite opp1 = new Opportunite();
        opp1.setId(1L);
        opp1.setClient(client);

        Opportunite opp2 = new Opportunite();
        opp2.setId(2L);
        opp2.setClient(client);

        when(opportuniteRepository.findByClientUtilisateurId(commercialUser.getId()))
                .thenReturn(Arrays.asList(opp1, opp2));

        List<Opportunite> result = opportuniteService.getOpportunitesByUser(commercialUser);

        assertEquals(2, result.size());
    }

    @Test
    void getOpportunitesByUser_AdminGetsAllOpportunities() {
        Opportunite opp1 = new Opportunite();
        opp1.setId(1L);
        opp1.setClient(client);

        Opportunite opp2 = new Opportunite();
        opp2.setId(2L);
        opp2.setClient(otherClient);

        when(opportuniteRepository.findAll()).thenReturn(Arrays.asList(opp1, opp2));

        List<Opportunite> result = opportuniteService.getOpportunitesByUser(adminUser);

        assertEquals(2, result.size());
    }

    @Test
    void getOpportuniteById_Success() {
        when(opportuniteRepository.findById(opportunite.getId())).thenReturn(Optional.of(opportunite));

        Opportunite result = opportuniteService.getOpportuniteById(opportunite.getId());

        assertNotNull(result);
        assertEquals(opportunite.getId(), result.getId());
        assertEquals(opportunite.getTitre(), result.getTitre());
    }

    @Test
    void getOpportuniteById_NotFound_ThrowsException() {
        when(opportuniteRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(OpportuniteNonTrouveException.class, () -> {
            opportuniteService.getOpportuniteById(999L);
        });
    }

    @Test
    void getOpportuniteForUser_Success() {
        when(opportuniteRepository.findById(opportunite.getId())).thenReturn(Optional.of(opportunite));

        Opportunite result = opportuniteService.getOpportuniteForUser(opportunite.getId(), commercialUser);

        assertNotNull(result);
        assertEquals(opportunite.getId(), result.getId());
    }

    @Test
    void getOpportuniteForUser_Unauthorized_ThrowsException() {
        opportunite.setClient(otherClient);
        when(opportuniteRepository.findById(opportunite.getId())).thenReturn(Optional.of(opportunite));

        assertThrows(AccesNonAutoriseException.class, () -> {
            opportuniteService.getOpportuniteForUser(opportunite.getId(), commercialUser);
        });
    }

    @Test
    void getOpportuniteForUser_AdminCanAccessAnyOpportunity() {
        opportunite.setClient(otherClient);
        when(opportuniteRepository.findById(opportunite.getId())).thenReturn(Optional.of(opportunite));

        Opportunite result = opportuniteService.getOpportuniteForUser(opportunite.getId(), adminUser);

        assertNotNull(result);
    }

    @Test
    void updateOpportunite_Success() {
        when(opportuniteRepository.findById(opportunite.getId())).thenReturn(Optional.of(opportunite));
        when(opportuniteRepository.save(any(Opportunite.class))).thenReturn(opportunite);

        Opportunite result = opportuniteService.updateOpportunite(
                opportunite.getId(),
                "Updated Title",
                BigDecimal.valueOf(2000),
                StatutOpportunite.EN_COURS,
                commercialUser
        );

        assertNotNull(result);
        assertEquals("Updated Title", result.getTitre());
        assertEquals(BigDecimal.valueOf(2000), result.getMontant());
        assertEquals(StatutOpportunite.EN_COURS, result.getStatut());
    }

    @Test
    void deleteOpportunite_Success() {
        when(opportuniteRepository.findById(opportunite.getId())).thenReturn(Optional.of(opportunite));

        opportuniteService.deleteOpportunite(opportunite.getId(), commercialUser);

        verify(opportuniteRepository).delete(opportunite);
    }

    @Test
    void getOpportunitesByStatut_CommercialGetsOnlyOwnOpportunities() {
        when(opportuniteRepository.findByClientUtilisateurIdAndStatut(commercialUser.getId(), StatutOpportunite.OUVERTE))
                .thenReturn(Arrays.asList(opportunite));

        List<Opportunite> result = opportuniteService.getOpportunitesByStatut(commercialUser, StatutOpportunite.OUVERTE);

        assertEquals(1, result.size());
    }

    @Test
    void getOpportunitesByStatut_AdminGetsAllMatchingOpportunities() {
        when(opportuniteRepository.findByStatut(StatutOpportunite.OUVERTE))
                .thenReturn(Arrays.asList(opportunite));

        List<Opportunite> result = opportuniteService.getOpportunitesByStatut(adminUser, StatutOpportunite.OUVERTE);

        assertEquals(1, result.size());
    }

    @Test
    void getPipelineByUser_ReturnsOpenAndInProgressOpportunities() {
        List<StatutOpportunite> pipelineStatuts = Arrays.asList(StatutOpportunite.OUVERTE, StatutOpportunite.EN_COURS);
        when(opportuniteRepository.findByClientUtilisateurIdAndStatutIn(commercialUser.getId(), pipelineStatuts))
                .thenReturn(Arrays.asList(opportunite));

        List<Opportunite> result = opportuniteService.getPipelineByUser(commercialUser);

        assertEquals(1, result.size());
    }
}