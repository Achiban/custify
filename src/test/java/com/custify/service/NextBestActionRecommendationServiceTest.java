package com.custify.service;

import com.custify.dto.NextBestActionDTO;
import com.custify.model.Client;
import com.custify.model.Interaction;
import com.custify.model.Opportunite;
import com.custify.model.Prospect;
import com.custify.model.Utilisateur;
import com.custify.model.enums.Role;
import com.custify.model.enums.StatutOpportunite;
import com.custify.model.enums.StatutProspect;
import com.custify.model.enums.TypeInteraction;
import com.custify.repository.InteractionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NextBestActionRecommendationServiceTest {

    @Mock
    private InteractionRepository interactionRepository;

    private NextBestActionRecommendationService service;

    private Utilisateur user;
    private Client client;
    private Opportunite opportunite;
    private Prospect prospect;

    @BeforeEach
    void setUp() {
        service = new NextBestActionRecommendationService();
        org.springframework.test.util.ReflectionTestUtils.setField(
                service, "interactionRepository", interactionRepository);

        user = new Utilisateur();
        user.setId(1L);
        user.setRole(Role.COMMERCIAL);

        client = new Client();
        client.setId(10L);
        client.setNom("Test Client");
        client.setUtilisateur(user);

        opportunite = new Opportunite();
        opportunite.setId(100L);
        opportunite.setTitre("Test Opportunity");
        opportunite.setClient(client);

        prospect = new Prospect();
        prospect.setId(200L);
        prospect.setNom("Test Prospect");
        prospect.setUtilisateur(user);
    }

    // === Tests pour Opportunites - Statut OUVERTE ===

    @Test
    void getNextBestAction_OpportuniteOuverte_SansInteraction_RetourneQualification() {
        opportunite.setStatut(StatutOpportunite.OUVERTE);
        when(interactionRepository.findByClientId(client.getId())).thenReturn(Collections.emptyList());

        NextBestActionDTO action = service.getNextBestActionForOpportunite(opportunite);

        assertNotNull(action);
        assertEquals("Qualifier l'opportunite", action.getTitre());
        assertEquals("REUNION", action.getTypeAction());
        assertEquals("HIGH", action.getPriorite());
        assertEquals("fa-users", action.getIcone());
    }

    @Test
    void getNextBestAction_OpportuniteOuverte_InteractionOld_RetourneRelance() {
        opportunite.setStatut(StatutOpportunite.OUVERTE);

        Interaction oldInteraction = new Interaction();
        oldInteraction.setDateHeure(LocalDateTime.now().minusDays(10));
        oldInteraction.setType(TypeInteraction.APPEL);

        when(interactionRepository.findByClientId(client.getId()))
                .thenReturn(List.of(oldInteraction));

        NextBestActionDTO action = service.getNextBestActionForOpportunite(opportunite);

        assertNotNull(action);
        assertEquals("Relancer le prospect", action.getTitre());
        assertEquals("APPEL", action.getTypeAction());
        assertEquals("HIGH", action.getPriorite());
    }

    @Test
    void getNextBestAction_OpportuniteOuverte_InteractionRecent() {
        opportunite.setStatut(StatutOpportunite.OUVERTE);

        Interaction recentInteraction = new Interaction();
        recentInteraction.setDateHeure(LocalDateTime.now().minusDays(2));

        when(interactionRepository.findByClientId(client.getId()))
                .thenReturn(List.of(recentInteraction));

        NextBestActionDTO action = service.getNextBestActionForOpportunite(opportunite);

        assertNotNull(action);
        assertEquals("Qualifier l'opportunite", action.getTitre());
    }

    // === Tests pour Opportunites - Statut EN_COURS ===

    @Test
    void getNextBestAction_OpportuniteEnCours_SansInteraction_RetourneProposition() {
        opportunite.setStatut(StatutOpportunite.EN_COURS);
        when(interactionRepository.findByClientId(client.getId()))
                .thenReturn(Collections.emptyList());

        NextBestActionDTO action = service.getNextBestActionForOpportunite(opportunite);

        assertNotNull(action);
        assertEquals("Preparer la proposition", action.getTitre());
        assertEquals("EMAIL", action.getTypeAction());
        assertEquals("MEDIUM", action.getPriorite());
    }

    @Test
    void getNextBestAction_OpportuniteEnCours_InteractionRecente() {
        opportunite.setStatut(StatutOpportunite.EN_COURS);

        Interaction recentInteraction = new Interaction();
        recentInteraction.setDateHeure(LocalDateTime.now().minusDays(3));

        when(interactionRepository.findByClientId(client.getId()))
                .thenReturn(List.of(recentInteraction));

        NextBestActionDTO action = service.getNextBestActionForOpportunite(opportunite);

        assertNotNull(action);
        assertTrue(action.getTitre().contains("proposition"));
    }

    @Test
    void getNextBestAction_OpportuniteEnCours_InteractionOldPlusDe7Jours() {
        opportunite.setStatut(StatutOpportunite.EN_COURS);

        Interaction oldInteraction = new Interaction();
        oldInteraction.setDateHeure(LocalDateTime.now().minusDays(8));

        when(interactionRepository.findByClientId(client.getId()))
                .thenReturn(List.of(oldInteraction));

        NextBestActionDTO action = service.getNextBestActionForOpportunite(opportunite);

        assertNotNull(action);
        assertTrue(action.getTitre().contains("email"));
        assertEquals("MEDIUM", action.getPriorite());
    }

    @Test
    void getNextBestAction_OpportuniteEnCours_InteractionVeryOldPlusDe14Jours() {
        opportunite.setStatut(StatutOpportunite.EN_COURS);

        Interaction veryOldInteraction = new Interaction();
        veryOldInteraction.setDateHeure(LocalDateTime.now().minusDays(20));

        when(interactionRepository.findByClientId(client.getId()))
                .thenReturn(List.of(veryOldInteraction));

        NextBestActionDTO action = service.getNextBestActionForOpportunite(opportunite);

        assertNotNull(action);
        assertTrue(action.getTitre().contains("Relance"));
        assertEquals("APPEL", action.getTypeAction());
        assertEquals("HIGH", action.getPriorite());
    }

    // === Tests pour Opportunites - Statut GAGNEE ===

    @Test
    void getNextBestAction_OpportuniteGagnee() {
        opportunite.setStatut(StatutOpportunite.GAGNEE);
        when(interactionRepository.findByClientId(client.getId())).thenReturn(Collections.emptyList());

        NextBestActionDTO action = service.getNextBestActionForOpportunite(opportunite);

        assertNotNull(action);
        assertEquals("MEDIUM", action.getPriorite());
    }

    // === Tests pour Opportunites - Statut PERDUE ===

    @Test
    void getNextBestAction_OpportunitePerdue() {
        opportunite.setStatut(StatutOpportunite.PERDUE);
        when(interactionRepository.findByClientId(client.getId())).thenReturn(Collections.emptyList());

        NextBestActionDTO action = service.getNextBestActionForOpportunite(opportunite);

        assertNotNull(action);
        assertEquals("LOW", action.getPriorite());
        assertEquals("REUNION", action.getTypeAction());
    }

    // === Tests pour Prospects ===

    @Test
    void getNextBestAction_ProspectNouveau() {
        prospect.setStatut(StatutProspect.NOUVEAU);

        NextBestActionDTO action = service.getNextBestActionForProspect(prospect);

        assertNotNull(action);
        assertTrue(action.getTitre().contains("contact"));
        assertEquals("HIGH", action.getPriorite());
        assertEquals("APPEL", action.getTypeAction());
    }

    @Test
    void getNextBestAction_ProspectContacte() {
        prospect.setStatut(StatutProspect.CONTACTE);

        NextBestActionDTO action = service.getNextBestActionForProspect(prospect);

        assertNotNull(action);
        assertTrue(action.getTitre().toLowerCase().contains("qualif"));
        assertEquals("HIGH", action.getPriorite());
    }

    @Test
    void getNextBestAction_ProspectQualifie() {
        prospect.setStatut(StatutProspect.QUALIFIE);

        NextBestActionDTO action = service.getNextBestActionForProspect(prospect);

        assertNotNull(action);
        assertEquals("HIGH", action.getPriorite());
    }

    @Test
    void getNextBestAction_ProspectConverti() {
        prospect.setStatut(StatutProspect.CONVERTI);

        NextBestActionDTO action = service.getNextBestActionForProspect(prospect);

        assertNotNull(action);
        assertEquals("HIGH", action.getPriorite());
    }

    @Test
    void getNextBestAction_ProspectPerdu() {
        prospect.setStatut(StatutProspect.PERDU);

        NextBestActionDTO action = service.getNextBestActionForProspect(prospect);

        assertNotNull(action);
        assertEquals("LOW", action.getPriorite());
    }

    // === Tests pour cas limites ===

    @Test
    void getNextBestAction_NullOpportunite_RetourneDefault() {
        NextBestActionDTO action = service.getNextBestActionForOpportunite(null);

        assertNotNull(action);
        assertFalse(action.getTitre().isEmpty());
        assertEquals("APPEL", action.getTypeAction());
    }

    @Test
    void getNextBestAction_OpportuniteWithNullStatut_RetourneDefault() {
        opportunite.setStatut(null);

        NextBestActionDTO action = service.getNextBestActionForOpportunite(opportunite);

        assertNotNull(action);
        assertEquals("Prendre contact", action.getTitre());
    }

    @Test
    void getNextBestAction_NullProspect_RetourneDefault() {
        NextBestActionDTO action = service.getNextBestActionForProspect(null);

        assertNotNull(action);
        assertFalse(action.getTitre().isEmpty());
    }

    @Test
    void getNextBestAction_AllActionHaveColors() {
        // Verifier que toutes les actions retournent des couleurs
        opportunite.setStatut(StatutOpportunite.OUVERTE);
        when(interactionRepository.findByClientId(client.getId())).thenReturn(Collections.emptyList());

        NextBestActionDTO action1 = service.getNextBestActionForOpportunite(opportunite);
        assertNotNull(action1.getCouleur());
        assertTrue(action1.getCouleur().startsWith("#"));

        opportunite.setStatut(StatutOpportunite.EN_COURS);
        NextBestActionDTO action2 = service.getNextBestActionForOpportunite(opportunite);
        assertNotNull(action2.getCouleur());
        assertTrue(action2.getCouleur().startsWith("#"));

        opportunite.setStatut(StatutOpportunite.GAGNEE);
        NextBestActionDTO action3 = service.getNextBestActionForOpportunite(opportunite);
        assertNotNull(action3.getCouleur());
        assertTrue(action3.getCouleur().startsWith("#"));
    }

    @Test
    void getNextBestAction_MultipleInteractions_UsesLatest() {
        opportunite.setStatut(StatutOpportunite.OUVERTE);

        Interaction oldInteraction = new Interaction();
        oldInteraction.setDateHeure(LocalDateTime.now().minusDays(20));

        Interaction recentInteraction = new Interaction();
        recentInteraction.setDateHeure(LocalDateTime.now().minusDays(3));

        when(interactionRepository.findByClientId(client.getId()))
                .thenReturn(List.of(oldInteraction, recentInteraction));

        NextBestActionDTO action = service.getNextBestActionForOpportunite(opportunite);

        assertNotNull(action);
        // Devrait utiliser la date la plus recente, donc suggestion de qualification
        assertTrue(action.getTitre().contains("Qualifier"));
    }
}

