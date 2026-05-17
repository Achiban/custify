package com.custify.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.custify.dto.CreerOpportuniteMarketplaceRequest;
import com.custify.exception.AccesNonAutoriseException;
import com.custify.model.Opportunite;
import com.custify.model.Utilisateur;
import com.custify.model.enums.StatutOpportunite;
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
class OpportuniteMarketplaceServiceTest {

    @Mock
    private OpportuniteRepository opportuniteRepository;

    private OpportuniteMarketplaceService service;

    @BeforeEach
    void setUp() {
        service = new OpportuniteMarketplaceService(opportuniteRepository);
    }

    @Test
    void publierShouldCreateOpportuniteWithDisponibleStatus() {
        Utilisateur vendeur = utilisateur(1L);
        CreerOpportuniteMarketplaceRequest req = request("Titre", "Desc", BigDecimal.TEN, "Tech");

        when(opportuniteRepository.save(any(Opportunite.class))).thenAnswer(inv -> {
            Opportunite o = inv.getArgument(0);
            o.setId(10L);
            return o;
        });

        Opportunite result = service.publier(req, vendeur);

        assertEquals("Titre", result.getTitre());
        assertEquals(StatutOpportunite.DISPONIBLE, result.getStatut());
        assertEquals(vendeur, result.getClientVendeur());

        ArgumentCaptor<Opportunite> captor = ArgumentCaptor.forClass(Opportunite.class);
        verify(opportuniteRepository).save(captor.capture());
        assertEquals("Tech", captor.getValue().getCategorie());
    }

    @Test
    void listerDisponiblesShouldReturnOnlyAvailableOpportunites() {
        Opportunite o = opportunite(1L, StatutOpportunite.DISPONIBLE, utilisateur(1L));
        when(opportuniteRepository.findByStatut(StatutOpportunite.DISPONIBLE)).thenReturn(List.of(o));

        List<Opportunite> result = service.listerDisponibles();

        assertEquals(1, result.size());
        assertEquals(StatutOpportunite.DISPONIBLE, result.get(0).getStatut());
    }

    @Test
    void listerParVendeurShouldReturnVendeurOpportunites() {
        Utilisateur vendeur = utilisateur(2L);
        Opportunite o = opportunite(1L, StatutOpportunite.DISPONIBLE, vendeur);
        when(opportuniteRepository.findByClientVendeur(vendeur)).thenReturn(List.of(o));

        List<Opportunite> result = service.listerParVendeur(vendeur);

        assertEquals(1, result.size());
        assertEquals(vendeur, result.get(0).getClientVendeur());
    }

    @Test
    void trouverParIdShouldReturnOpportunite() {
        Opportunite o = opportunite(5L, StatutOpportunite.DISPONIBLE, utilisateur(1L));
        when(opportuniteRepository.findById(5L)).thenReturn(Optional.of(o));

        Opportunite result = service.trouverParId(5L);

        assertEquals(5L, result.getId());
    }

    @Test
    void trouverParIdShouldThrowWhenNotFound() {
        when(opportuniteRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.trouverParId(99L));
        assertEquals(true, ex.getMessage().contains("99"));
    }

    @Test
    void modifierShouldUpdateOpportuniteFields() {
        Utilisateur vendeur = utilisateur(1L);
        Opportunite opp = opportunite(3L, StatutOpportunite.DISPONIBLE, vendeur);
        when(opportuniteRepository.findById(3L)).thenReturn(Optional.of(opp));
        when(opportuniteRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CreerOpportuniteMarketplaceRequest req = request("Nouveau titre", "Nouvelle desc", BigDecimal.ONE, "Finance");
        Opportunite result = service.modifier(3L, req, vendeur);

        assertEquals("Nouveau titre", result.getTitre());
        assertEquals("Nouvelle desc", result.getDescriptionComplete());
    }

    @Test
    void modifierShouldThrowWhenNotVendeur() {
        Utilisateur vendeur = utilisateur(1L);
        Utilisateur autre = utilisateur(2L);
        Opportunite opp = opportunite(3L, StatutOpportunite.DISPONIBLE, vendeur);
        when(opportuniteRepository.findById(3L)).thenReturn(Optional.of(opp));

        CreerOpportuniteMarketplaceRequest req = request("T", "D", BigDecimal.ONE, null);

        assertThrows(AccesNonAutoriseException.class, () -> service.modifier(3L, req, autre));
        verify(opportuniteRepository, never()).save(any());
    }

    @Test
    void modifierShouldThrowWhenNotDisponible() {
        Utilisateur vendeur = utilisateur(1L);
        Opportunite opp = opportunite(3L, StatutOpportunite.ATTRIBUEE, vendeur);
        when(opportuniteRepository.findById(3L)).thenReturn(Optional.of(opp));

        CreerOpportuniteMarketplaceRequest req = request("T", "D", BigDecimal.ONE, null);

        assertThrows(IllegalStateException.class, () -> service.modifier(3L, req, vendeur));
        verify(opportuniteRepository, never()).save(any());
    }

    @Test
    void supprimerShouldDeleteOpportunite() {
        Utilisateur vendeur = utilisateur(1L);
        Opportunite opp = opportunite(4L, StatutOpportunite.DISPONIBLE, vendeur);
        when(opportuniteRepository.findById(4L)).thenReturn(Optional.of(opp));

        service.supprimer(4L, vendeur);

        verify(opportuniteRepository).delete(opp);
    }

    @Test
    void supprimerShouldThrowWhenNotVendeur() {
        Utilisateur vendeur = utilisateur(1L);
        Utilisateur autre = utilisateur(2L);
        Opportunite opp = opportunite(4L, StatutOpportunite.DISPONIBLE, vendeur);
        when(opportuniteRepository.findById(4L)).thenReturn(Optional.of(opp));

        assertThrows(AccesNonAutoriseException.class, () -> service.supprimer(4L, autre));
        verify(opportuniteRepository, never()).delete(any());
    }

    @Test
    void supprimerShouldThrowWhenNotDisponible() {
        Utilisateur vendeur = utilisateur(1L);
        Opportunite opp = opportunite(4L, StatutOpportunite.CONCLUE, vendeur);
        when(opportuniteRepository.findById(4L)).thenReturn(Optional.of(opp));

        assertThrows(IllegalStateException.class, () -> service.supprimer(4L, vendeur));
        verify(opportuniteRepository, never()).delete(any());
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
        o.setTitre("Titre");
        o.setDescriptionComplete("Desc");
        o.setMontant(BigDecimal.TEN);
        return o;
    }

    private CreerOpportuniteMarketplaceRequest request(String titre, String desc, BigDecimal montant, String categorie) {
        CreerOpportuniteMarketplaceRequest r = new CreerOpportuniteMarketplaceRequest();
        r.setTitre(titre);
        r.setDescriptionComplete(desc);
        r.setMontant(montant);
        r.setCategorie(categorie);
        return r;
    }
}
