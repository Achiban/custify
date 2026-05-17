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

    @Mock private DemandeOpportuniteRepository demandeRepository;
    @Mock private OpportuniteRepository opportuniteRepository;
    @Mock private AffectationRepository affectationRepository;

    private DemandeService service;

    @BeforeEach
    void setUp() {
        service = new DemandeService(demandeRepository, opportuniteRepository, affectationRepository);
    }

    @Test
    void creerDemandeShouldCreateDemandeEnAttente() {
        Utilisateur vendeur = utilisateur(1L);
        Utilisateur client = utilisateur(2L);
        Opportunite opp = opportunite(10L, StatutOpportunite.DISPONIBLE, vendeur);

        when(opportuniteRepository.findById(10L)).thenReturn(Optional.of(opp));
        when(demandeRepository.existsByClientDemandeurAndOpportunite(client, opp)).thenReturn(false);
        when(demandeRepository.save(any())).thenAnswer(inv -> {
            DemandeOpportunite d = inv.getArgument(0);
            d.setId(99L);
            return d;
        });

        DemandeOpportunite result = service.creerDemande(10L, client);

        assertEquals(StatutDemande.EN_ATTENTE, result.getStatut());
        assertEquals(client, result.getClientDemandeur());
        assertEquals(opp, result.getOpportunite());
    }

    @Test
    void creerDemandeShouldThrowWhenOpportuniteNotFound() {
        when(opportuniteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> service.creerDemande(99L, utilisateur(2L)));
    }

    @Test
    void creerDemandeShouldThrowWhenOpportuniteNotDisponible() {
        Utilisateur vendeur = utilisateur(1L);
        Opportunite opp = opportunite(10L, StatutOpportunite.ATTRIBUEE, vendeur);
        when(opportuniteRepository.findById(10L)).thenReturn(Optional.of(opp));

        assertThrows(IllegalStateException.class, () -> service.creerDemande(10L, utilisateur(2L)));
    }

    @Test
    void creerDemandeShouldThrowWhenClientIsVendeur() {
        Utilisateur vendeur = utilisateur(1L);
        Opportunite opp = opportunite(10L, StatutOpportunite.DISPONIBLE, vendeur);
        when(opportuniteRepository.findById(10L)).thenReturn(Optional.of(opp));

        assertThrows(IllegalStateException.class, () -> service.creerDemande(10L, vendeur));
    }

    @Test
    void creerDemandeShouldThrowWhenAlreadyDemanded() {
        Utilisateur vendeur = utilisateur(1L);
        Utilisateur client = utilisateur(2L);
        Opportunite opp = opportunite(10L, StatutOpportunite.DISPONIBLE, vendeur);

        when(opportuniteRepository.findById(10L)).thenReturn(Optional.of(opp));
        when(demandeRepository.existsByClientDemandeurAndOpportunite(client, opp)).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> service.creerDemande(10L, client));
        verify(demandeRepository, never()).save(any());
    }

    @Test
    void listerEnAttenteShouldReturnPendingDemandes() {
        DemandeOpportunite d = new DemandeOpportunite();
        d.setStatut(StatutDemande.EN_ATTENTE);
        when(demandeRepository.findByStatut(StatutDemande.EN_ATTENTE)).thenReturn(List.of(d));

        List<DemandeOpportunite> result = service.listerEnAttente();

        assertEquals(1, result.size());
        assertEquals(StatutDemande.EN_ATTENTE, result.get(0).getStatut());
    }

    @Test
    void accepterDemandeShouldAcceptMarkOpportuniteConclueAndCreateAffectation() {
        Utilisateur commercial = utilisateur(1L);
        Utilisateur client = utilisateur(2L);
        Opportunite opp = opportunite(10L, StatutOpportunite.DISPONIBLE, utilisateur(3L));
        DemandeOpportunite demande = demande(20L, client, opp, StatutDemande.EN_ATTENTE);

        when(demandeRepository.findById(20L)).thenReturn(Optional.of(demande));
        when(demandeRepository.save(any())).thenReturn(demande);
        when(opportuniteRepository.save(any())).thenReturn(opp);
        when(affectationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.accepterDemande(20L, commercial);

        assertEquals(StatutDemande.ACCEPTEE, demande.getStatut());
        assertEquals(StatutOpportunite.CONCLUE, opp.getStatut());

        ArgumentCaptor<Affectation> captor = ArgumentCaptor.forClass(Affectation.class);
        verify(affectationRepository).save(captor.capture());
        assertEquals(commercial, captor.getValue().getCommercial());
        assertEquals(client, captor.getValue().getClientBeneficiaire());
    }

    @Test
    void accepterDemandeShouldThrowWhenAlreadyTreated() {
        Utilisateur commercial = utilisateur(1L);
        Opportunite opp = opportunite(10L, StatutOpportunite.CONCLUE, utilisateur(3L));
        DemandeOpportunite demande = demande(20L, utilisateur(2L), opp, StatutDemande.ACCEPTEE);

        when(demandeRepository.findById(20L)).thenReturn(Optional.of(demande));

        assertThrows(IllegalStateException.class, () -> service.accepterDemande(20L, commercial));
    }

    @Test
    void refuserDemandeShouldSetRefusee() {
        Opportunite opp = opportunite(10L, StatutOpportunite.DISPONIBLE, utilisateur(1L));
        DemandeOpportunite demande = demande(20L, utilisateur(2L), opp, StatutDemande.EN_ATTENTE);

        when(demandeRepository.findById(20L)).thenReturn(Optional.of(demande));
        when(demandeRepository.save(any())).thenReturn(demande);

        service.refuserDemande(20L);

        assertEquals(StatutDemande.REFUSEE, demande.getStatut());
        verify(demandeRepository).save(demande);
    }

    @Test
    void refuserDemandeShouldThrowWhenAlreadyTreated() {
        Opportunite opp = opportunite(10L, StatutOpportunite.CONCLUE, utilisateur(1L));
        DemandeOpportunite demande = demande(20L, utilisateur(2L), opp, StatutDemande.REFUSEE);

        when(demandeRepository.findById(20L)).thenReturn(Optional.of(demande));

        assertThrows(IllegalStateException.class, () -> service.refuserDemande(20L));
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private Utilisateur utilisateur(Long id) {
        Utilisateur u = new Utilisateur();
        u.setId(id);
        return u;
    }

    private Opportunite opportunite(Long id, StatutOpportunite statut, Utilisateur vendeur) {
        Opportunite o = new Opportunite();
        o.setId(id);
        o.setStatut(statut);
        o.setClientVendeur(vendeur);
        o.setTitre("T");
        o.setDescriptionComplete("D");
        o.setMontant(BigDecimal.TEN);
        return o;
    }

    private DemandeOpportunite demande(Long id, Utilisateur client, Opportunite opp, StatutDemande statut) {
        DemandeOpportunite d = new DemandeOpportunite();
        d.setId(id);
        d.setClientDemandeur(client);
        d.setOpportunite(opp);
        d.setStatut(statut);
        return d;
    }
}
