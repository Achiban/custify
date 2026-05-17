package com.custify.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
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

    private Utilisateur vendeur;
    private CreerOpportuniteMarketplaceRequest request;

    @BeforeEach
    void setUp() {
        service = new OpportuniteMarketplaceService(opportuniteRepository);

        vendeur = new Utilisateur();
        vendeur.setId(1L);

        request = new CreerOpportuniteMarketplaceRequest();
        request.setTitre("Mission CRM");
        request.setDescriptionComplete("Intégration Salesforce");
        request.setMontant(BigDecimal.valueOf(12000));
        request.setCategorie("IT");
    }

    @Test
    void publierShouldSaveOpportuniteWithStatutDISPONIBLE() {
        when(opportuniteRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.publier(request, vendeur);

        ArgumentCaptor<Opportunite> captor = ArgumentCaptor.forClass(Opportunite.class);
        verify(opportuniteRepository).save(captor.capture());
        Opportunite saved = captor.getValue();

        assertEquals("Mission CRM", saved.getTitre());
        assertEquals(StatutOpportunite.DISPONIBLE, saved.getStatut());
        assertEquals(vendeur, saved.getClientVendeur());
        assertEquals(BigDecimal.valueOf(12000), saved.getMontant());
    }

    @Test
    void modifierShouldUpdateFieldsWhenVendeurIsOwnerAndStatutDISPONIBLE() {
        Opportunite opp = new Opportunite();
        opp.setId(5L);
        opp.setTitre("Ancien titre");
        opp.setStatut(StatutOpportunite.DISPONIBLE);
        opp.setClientVendeur(vendeur);
        opp.setMontant(BigDecimal.ZERO);
        opp.setDescriptionComplete("Ancienne desc");

        when(opportuniteRepository.findById(5L)).thenReturn(Optional.of(opp));
        when(opportuniteRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.modifier(5L, request, vendeur);

        assertEquals("Mission CRM", opp.getTitre());
        assertEquals("Intégration Salesforce", opp.getDescriptionComplete());
        assertEquals(BigDecimal.valueOf(12000), opp.getMontant());
    }

    @Test
    void modifierShouldThrowWhenVendeurIsNotOwner() {
        Utilisateur autreVendeur = new Utilisateur();
        autreVendeur.setId(99L);

        Opportunite opp = new Opportunite();
        opp.setId(5L);
        opp.setStatut(StatutOpportunite.DISPONIBLE);
        opp.setClientVendeur(autreVendeur);

        when(opportuniteRepository.findById(5L)).thenReturn(Optional.of(opp));

        assertThrows(AccesNonAutoriseException.class, () -> service.modifier(5L, request, vendeur));
    }

    @Test
    void modifierShouldThrowWhenOpportuniteNotDISPONIBLE() {
        Opportunite opp = new Opportunite();
        opp.setId(5L);
        opp.setStatut(StatutOpportunite.ATTRIBUEE);
        opp.setClientVendeur(vendeur);

        when(opportuniteRepository.findById(5L)).thenReturn(Optional.of(opp));

        assertThrows(IllegalStateException.class, () -> service.modifier(5L, request, vendeur));
    }

    @Test
    void supprimerShouldDeleteWhenOwnerAndDISPONIBLE() {
        Opportunite opp = new Opportunite();
        opp.setId(7L);
        opp.setStatut(StatutOpportunite.DISPONIBLE);
        opp.setClientVendeur(vendeur);

        when(opportuniteRepository.findById(7L)).thenReturn(Optional.of(opp));

        service.supprimer(7L, vendeur);

        verify(opportuniteRepository).delete(opp);
    }

    @Test
    void supprimerShouldThrowWhenNotOwner() {
        Utilisateur autreVendeur = new Utilisateur();
        autreVendeur.setId(99L);

        Opportunite opp = new Opportunite();
        opp.setId(7L);
        opp.setStatut(StatutOpportunite.DISPONIBLE);
        opp.setClientVendeur(autreVendeur);

        when(opportuniteRepository.findById(7L)).thenReturn(Optional.of(opp));

        assertThrows(AccesNonAutoriseException.class, () -> service.supprimer(7L, vendeur));
    }

    @Test
    void listerDisponiblesShouldDelegateToRepository() {
        when(opportuniteRepository.findByStatut(StatutOpportunite.DISPONIBLE)).thenReturn(List.of());

        List<Opportunite> result = service.listerDisponibles();

        assertEquals(0, result.size());
        verify(opportuniteRepository).findByStatut(StatutOpportunite.DISPONIBLE);
    }
}
