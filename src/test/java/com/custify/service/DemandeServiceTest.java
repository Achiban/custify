package com.custify.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.custify.model.Affectation;
import com.custify.model.DemandeOpportunite;
import com.custify.model.Opportunite;
import com.custify.model.Utilisateur;
import com.custify.model.enums.StatutAffectation;
import com.custify.model.enums.StatutDemande;
import com.custify.model.enums.StatutOpportunite;
import com.custify.repository.AffectationRepository;
import com.custify.repository.DemandeOpportuniteRepository;
import com.custify.repository.OpportuniteRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DemandeServiceTest {

    @Mock
    private DemandeOpportuniteRepository demandeRepository;

    @Mock
    private OpportuniteRepository opportuniteRepository;

    @Mock
    private AffectationRepository affectationRepository;

    private DemandeService demandeService;

    private Utilisateur vendeur;
    private Utilisateur acheteur;
    private Utilisateur commercial;
    private Opportunite opportunite;

    @BeforeEach
    void setUp() {
        demandeService = new DemandeService(demandeRepository, opportuniteRepository, affectationRepository);

        vendeur = new Utilisateur();
        vendeur.setId(1L);

        acheteur = new Utilisateur();
        acheteur.setId(2L);

        commercial = new Utilisateur();
        commercial.setId(3L);

        opportunite = new Opportunite();
        opportunite.setId(10L);
        opportunite.setTitre("Opportunité test");
        opportunite.setMontant(BigDecimal.valueOf(5000));
        opportunite.setDescriptionComplete("Description");
        opportunite.setStatut(StatutOpportunite.DISPONIBLE);
        opportunite.setClientVendeur(vendeur);
    }

    @Test
    void creerDemandeShouldSaveAndReturnDemande() {
        when(opportuniteRepository.findById(10L)).thenReturn(Optional.of(opportunite));
        when(demandeRepository.existsByClientDemandeurAndOpportunite(acheteur, opportunite)).thenReturn(false);
        when(demandeRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        DemandeOpportunite result = demandeService.creerDemande(10L, acheteur);

        assertEquals(StatutDemande.EN_ATTENTE, result.getStatut());
        assertEquals(acheteur, result.getClientDemandeur());
        assertEquals(opportunite, result.getOpportunite());
    }

    @Test
    void creerDemandeShouldThrowWhenOpportuniteNotDisponible() {
        opportunite.setStatut(StatutOpportunite.ATTRIBUEE);
        when(opportuniteRepository.findById(10L)).thenReturn(Optional.of(opportunite));

        assertThrows(IllegalStateException.class, () -> demandeService.creerDemande(10L, acheteur));
        verify(demandeRepository, never()).save(any());
    }

    @Test
    void creerDemandeShouldThrowWhenClientIsVendeur() {
        when(opportuniteRepository.findById(10L)).thenReturn(Optional.of(opportunite));

        assertThrows(IllegalStateException.class, () -> demandeService.creerDemande(10L, vendeur));
        verify(demandeRepository, never()).save(any());
    }

    @Test
    void creerDemandeShouldThrowWhenDemandeAlreadyExists() {
        when(opportuniteRepository.findById(10L)).thenReturn(Optional.of(opportunite));
        when(demandeRepository.existsByClientDemandeurAndOpportunite(acheteur, opportunite)).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> demandeService.creerDemande(10L, acheteur));
        verify(demandeRepository, never()).save(any());
    }

    @Test
    void accepterDemandeShouldSetOpportuniteATTRIBUEEAndAffectationENATTENTE() {
        DemandeOpportunite demande = new DemandeOpportunite();
        demande.setId(5L);
        demande.setClientDemandeur(acheteur);
        demande.setOpportunite(opportunite);
        demande.setStatut(StatutDemande.EN_ATTENTE);

        when(demandeRepository.findById(5L)).thenReturn(Optional.of(demande));
        when(demandeRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(opportuniteRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(affectationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        demandeService.accepterDemande(5L, commercial);

        assertEquals(StatutDemande.ACCEPTEE, demande.getStatut());

        ArgumentCaptor<Opportunite> oppCaptor = ArgumentCaptor.forClass(Opportunite.class);
        verify(opportuniteRepository).save(oppCaptor.capture());
        assertEquals(StatutOpportunite.ATTRIBUEE, oppCaptor.getValue().getStatut());

        ArgumentCaptor<Affectation> affCaptor = ArgumentCaptor.forClass(Affectation.class);
        verify(affectationRepository).save(affCaptor.capture());
        assertEquals(StatutAffectation.EN_ATTENTE, affCaptor.getValue().getStatutClient());
        assertEquals(commercial, affCaptor.getValue().getCommercial());
        assertEquals(acheteur, affCaptor.getValue().getClientBeneficiaire());
    }

    @Test
    void accepterDemandeShouldThrowWhenDemandeAlreadyTraitee() {
        DemandeOpportunite demande = new DemandeOpportunite();
        demande.setId(6L);
        demande.setStatut(StatutDemande.ACCEPTEE);
        demande.setOpportunite(opportunite);
        demande.setClientDemandeur(acheteur);

        when(demandeRepository.findById(6L)).thenReturn(Optional.of(demande));

        assertThrows(IllegalStateException.class, () -> demandeService.accepterDemande(6L, commercial));
        verify(affectationRepository, never()).save(any());
    }

    @Test
    void refuserDemandeShouldSetStatutREFUSEE() {
        DemandeOpportunite demande = new DemandeOpportunite();
        demande.setId(7L);
        demande.setStatut(StatutDemande.EN_ATTENTE);
        demande.setOpportunite(opportunite);
        demande.setClientDemandeur(acheteur);

        when(demandeRepository.findById(7L)).thenReturn(Optional.of(demande));
        when(demandeRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        demandeService.refuserDemande(7L);

        assertEquals(StatutDemande.REFUSEE, demande.getStatut());
        verify(opportuniteRepository, never()).save(any());
    }

    @Test
    void listerParClientShouldDelegateToRepository() {
        when(demandeRepository.findByClientDemandeur(acheteur)).thenReturn(List.of());

        List<DemandeOpportunite> result = demandeService.listerParClient(acheteur);

        assertEquals(0, result.size());
        verify(demandeRepository).findByClientDemandeur(acheteur);
    }
}
