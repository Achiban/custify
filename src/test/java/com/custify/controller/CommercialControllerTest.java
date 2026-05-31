package com.custify.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.custify.dto.CreerAffectationRequest;
import com.custify.model.Affectation;
import com.custify.model.Opportunite;
import com.custify.model.Utilisateur;
import com.custify.model.enums.Role;
import com.custify.model.enums.StatutAffectation;
import com.custify.model.enums.StatutOpportunite;
import com.custify.repository.UtilisateurRepository;
import com.custify.service.AffectationService;
import com.custify.service.DemandeService;
import com.custify.service.OpportuniteMarketplaceService;
import com.custify.service.ReunionService;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

@ExtendWith(MockitoExtension.class)
class CommercialControllerTest {

    @Mock private OpportuniteMarketplaceService opportuniteService;
    @Mock private DemandeService demandeService;
    @Mock private AffectationService affectationService;
    @Mock private UtilisateurRepository utilisateurRepository;
    @Mock private ReunionService reunionService;

    private CommercialController controller;

    @BeforeEach
    void setUp() {
        controller = new CommercialController(opportuniteService, demandeService,
                affectationService, utilisateurRepository, reunionService);
    }

    @Test
    void dashboardShouldAddStatsToModelAndReturnView() {
        Utilisateur commercial = utilisateur(1L, Role.COMMERCIAL, "comm@mail.com");
        UserDetails userDetails = user("comm@mail.com");

        when(utilisateurRepository.findByEmail("comm@mail.com")).thenReturn(Optional.of(commercial));
        when(demandeService.listerEnAttente()).thenReturn(List.of());
        when(affectationService.listerParCommercial(commercial)).thenReturn(List.of());
        when(reunionService.listerParCommercial(commercial)).thenReturn(List.of());

        ExtendedModelMap model = new ExtendedModelMap();
        String view = controller.dashboard(userDetails, model);

        assertEquals("commercial/dashboard", view);
        assertTrue(model.containsAttribute("commercial"));
    }

    @Test
    void demandesShouldAddDemandesAndOpportunitesToModel() {
        Utilisateur commercial = utilisateur(1L, Role.COMMERCIAL, "comm@mail.com");
        UserDetails userDetails = user("comm@mail.com");

        when(utilisateurRepository.findByEmail("comm@mail.com")).thenReturn(Optional.of(commercial));
        when(demandeService.listerEnAttente()).thenReturn(List.of());
        when(opportuniteService.listerDisponibles()).thenReturn(List.of());
        when(utilisateurRepository.findByRole(Role.CLIENT)).thenReturn(List.of());

        ExtendedModelMap model = new ExtendedModelMap();
        String view = controller.demandes(userDetails, model);

        assertEquals("commercial/demandes", view);
    }

    @Test
    void accepterDemandeShouldRedirectToDemandesWithSuccessMessage() {
        Utilisateur commercial = utilisateur(1L, Role.COMMERCIAL, "comm@mail.com");
        UserDetails userDetails = user("comm@mail.com");
        when(utilisateurRepository.findByEmail("comm@mail.com")).thenReturn(Optional.of(commercial));

        RedirectAttributesModelMap redirectAttrs = new RedirectAttributesModelMap();
        String view = controller.accepterDemande(10L, userDetails, redirectAttrs);

        assertEquals("redirect:/commercial/demandes", view);
        verify(demandeService).accepterDemande(10L, commercial);
        assertTrue(redirectAttrs.getFlashAttributes().containsKey("message"));
    }

    @Test
    void refuserDemandeShouldRedirectToDemandesWithMessage() {
        RedirectAttributesModelMap redirectAttrs = new RedirectAttributesModelMap();
        String view = controller.refuserDemande(10L, redirectAttrs);

        assertEquals("redirect:/commercial/demandes", view);
        verify(demandeService).refuserDemande(10L);
    }

    @Test
    void opportunitesShouldAddStatsAndReturnView() {
        Opportunite o1 = opportunite(1L);
        o1.setStatut(StatutOpportunite.DISPONIBLE);
        Opportunite o2 = opportunite(2L);
        o2.setStatut(StatutOpportunite.ATTRIBUEE);
        
        when(opportuniteService.listerToutes()).thenReturn(List.of(o1, o2));

        ExtendedModelMap model = new ExtendedModelMap();
        String view = controller.opportunites(model);

        assertEquals("commercial/opportunites", view);
        assertTrue(model.containsAttribute("toutesLesOpportunites"));
        assertEquals(1L, model.get("countDispo"));
        assertEquals(1L, model.get("countAttrib"));
        assertEquals(0L, model.get("countConclue"));
    }

    @Test
    void clientsShouldAddClientsToModelAndReturnView() {
        Utilisateur commercial = utilisateur(1L, Role.COMMERCIAL, "comm@mail.com");
        UserDetails userDetails = user("comm@mail.com");

        when(utilisateurRepository.findByEmail("comm@mail.com")).thenReturn(Optional.of(commercial));
        when(utilisateurRepository.findByRole(Role.CLIENT)).thenReturn(List.of());

        ExtendedModelMap model = new ExtendedModelMap();
        String view = controller.clients(userDetails, model);

        assertEquals("commercial/clients", view);
        assertTrue(model.containsAttribute("clients"));
    }

    @Test
    void affectationsShouldAddAffectationsAndReturnView() {
        Utilisateur commercial = utilisateur(1L, Role.COMMERCIAL, "comm@mail.com");
        UserDetails userDetails = user("comm@mail.com");

        when(utilisateurRepository.findByEmail("comm@mail.com")).thenReturn(Optional.of(commercial));
        when(affectationService.listerParCommercial(commercial)).thenReturn(List.of());
        when(opportuniteService.listerDisponibles()).thenReturn(List.of());
        when(utilisateurRepository.findByRole(Role.CLIENT)).thenReturn(List.of());

        ExtendedModelMap model = new ExtendedModelMap();
        String view = controller.affectations(userDetails, model);

        assertEquals("commercial/affectations", view);
    }

    @Test
    void reunionsShouldAddMesReunionsAndReturnView() {
        Utilisateur commercial = utilisateur(1L, Role.COMMERCIAL, "comm@mail.com");
        UserDetails userDetails = user("comm@mail.com");

        when(utilisateurRepository.findByEmail("comm@mail.com")).thenReturn(Optional.of(commercial));
        when(reunionService.listerParCommercial(commercial)).thenReturn(List.of());

        ExtendedModelMap model = new ExtendedModelMap();
        String view = controller.reunions(userDetails, model);

        assertEquals("commercial/reunions", view);
        assertTrue(model.containsAttribute("mesReunions"));
    }

    @Test
    void creerAffectationShouldRedirectOnSuccess() {
        Utilisateur commercial = utilisateur(1L, Role.COMMERCIAL, "comm@mail.com");
        UserDetails userDetails = user("comm@mail.com");
        when(utilisateurRepository.findByEmail("comm@mail.com")).thenReturn(Optional.of(commercial));

        CreerAffectationRequest req = new CreerAffectationRequest();
        req.setClientBeneficiaireId(2L);
        req.setOpportuniteId(10L);

        Affectation aff = new Affectation();
        when(affectationService.creerAffectation(any(), any())).thenReturn(aff);

        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(req, "affectationRequest");
        RedirectAttributesModelMap redirectAttrs = new RedirectAttributesModelMap();

        String view = controller.creerAffectation(req, bindingResult, userDetails, redirectAttrs, new ExtendedModelMap());

        assertEquals("redirect:/commercial/affectations", view);
        assertTrue(redirectAttrs.getFlashAttributes().containsKey("message"));
    }

    @Test
    void creerAffectationShouldReturnFormOnValidationError() {
        Utilisateur commercial = utilisateur(1L, Role.COMMERCIAL, "comm@mail.com");
        UserDetails userDetails = user("comm@mail.com");
        when(utilisateurRepository.findByEmail("comm@mail.com")).thenReturn(Optional.of(commercial));
        when(affectationService.listerParCommercial(commercial)).thenReturn(List.of());
        when(opportuniteService.listerDisponibles()).thenReturn(List.of());
        when(utilisateurRepository.findByRole(Role.CLIENT)).thenReturn(List.of());

        CreerAffectationRequest req = new CreerAffectationRequest();
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(req, "affectationRequest");
        bindingResult.rejectValue("clientBeneficiaireId", "required");

        String view = controller.creerAffectation(req, bindingResult, userDetails,
                new RedirectAttributesModelMap(), new ExtendedModelMap());

        assertEquals("commercial/affectations", view);
    }

    @Test
    void supprimerClientShouldDeleteAndRedirect() {
        Utilisateur client = utilisateur(3L, Role.CLIENT, "client@mail.com");
        when(utilisateurRepository.findById(3L)).thenReturn(Optional.of(client));

        RedirectAttributesModelMap redirectAttrs = new RedirectAttributesModelMap();
        String view = controller.supprimerClient(3L, redirectAttrs);

        assertEquals("redirect:/commercial/clients", view);
        verify(utilisateurRepository).delete(client);
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private Utilisateur utilisateur(Long id, Role role, String email) {
        Utilisateur u = new Utilisateur();
        u.setId(id);
        u.setRole(role);
        u.setEmail(email);
        u.setNom("User " + id);
        return u;
    }

    private UserDetails user(String email) {
        return User.withUsername(email).password("pw").roles("COMMERCIAL").build();
    }

    private Opportunite opportunite(Long id) {
        Opportunite o = new Opportunite();
        o.setId(id);
        o.setStatut(StatutOpportunite.DISPONIBLE);
        o.setTitre("T");
        o.setDescriptionComplete("D");
        o.setMontant(BigDecimal.TEN);
        return o;
    }

    private Affectation affectation(Long id, Utilisateur client, Opportunite opp) {
        Affectation a = new Affectation();
        a.setId(id);
        a.setClientBeneficiaire(client);
        a.setOpportunite(opp);
        a.setStatutClient(StatutAffectation.EN_ATTENTE);
        return a;
    }
}
