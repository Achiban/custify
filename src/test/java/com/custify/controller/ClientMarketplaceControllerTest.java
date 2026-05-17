package com.custify.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.custify.dto.CreerOpportuniteMarketplaceRequest;
import com.custify.model.Opportunite;
import com.custify.model.Utilisateur;
import com.custify.model.enums.Role;
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
class ClientMarketplaceControllerTest {

    @Mock private OpportuniteMarketplaceService opportuniteService;
    @Mock private DemandeService demandeService;
    @Mock private AffectationService affectationService;
    @Mock private UtilisateurRepository utilisateurRepository;
    @Mock private ReunionService reunionService;

    private ClientMarketplaceController controller;

    @BeforeEach
    void setUp() {
        controller = new ClientMarketplaceController(opportuniteService, demandeService,
                affectationService, utilisateurRepository, reunionService);
    }

    @Test
    void dashboardShouldAddAllDataToModelAndReturnView() {
        Utilisateur client = utilisateur(1L, "client@mail.com");
        UserDetails userDetails = user("client@mail.com");

        when(utilisateurRepository.findByEmail("client@mail.com")).thenReturn(Optional.of(client));
        when(opportuniteService.listerParVendeur(client)).thenReturn(List.of());
        when(opportuniteService.listerDisponibles()).thenReturn(List.of());
        when(demandeService.listerParClient(client)).thenReturn(List.of());
        when(affectationService.listerParClient(client)).thenReturn(List.of());
        when(reunionService.listerParClient(client)).thenReturn(List.of());

        ExtendedModelMap model = new ExtendedModelMap();
        String view = controller.dashboard(userDetails, model);

        assertEquals("client/dashboard", view);
        assertTrue(model.containsAttribute("client"));
        assertTrue(model.containsAttribute("mesOpportunites"));
        assertTrue(model.containsAttribute("mesDemandes"));
        assertTrue(model.containsAttribute("mesAffectations"));
    }

    @Test
    void formulaireNouvelleOpportuniteShouldReturnFormView() {
        ExtendedModelMap model = new ExtendedModelMap();
        String view = controller.formulaireNouvelleOpportunite(model);

        assertEquals("client/opportunite-form", view);
        assertTrue(model.containsAttribute("opportuniteRequest"));
    }

    @Test
    void publierOpportuniteShouldRedirectToDashboardOnSuccess() {
        Utilisateur client = utilisateur(1L, "client@mail.com");
        UserDetails userDetails = user("client@mail.com");
        when(utilisateurRepository.findByEmail("client@mail.com")).thenReturn(Optional.of(client));

        CreerOpportuniteMarketplaceRequest req = request("Titre", "Desc", BigDecimal.TEN);
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(req, "opportuniteRequest");
        RedirectAttributesModelMap redirectAttrs = new RedirectAttributesModelMap();

        String view = controller.publierOpportunite(req, bindingResult, userDetails, redirectAttrs);

        assertEquals("redirect:/client/dashboard", view);
        verify(opportuniteService).publier(any(), eq(client));
        assertTrue(redirectAttrs.getFlashAttributes().containsKey("message"));
    }

    @Test
    void publierOpportuniteShouldReturnFormOnValidationError() {
        CreerOpportuniteMarketplaceRequest req = new CreerOpportuniteMarketplaceRequest();
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(req, "opportuniteRequest");
        bindingResult.rejectValue("titre", "required");

        String view = controller.publierOpportunite(req, bindingResult, user("x@mail.com"), new RedirectAttributesModelMap());

        assertEquals("client/opportunite-form", view);
    }

    @Test
    void formulaireModificationShouldReturnFormWhenClientIsVendeur() {
        Utilisateur client = utilisateur(1L, "client@mail.com");
        UserDetails userDetails = user("client@mail.com");
        Opportunite opp = opportunite(10L, client);

        when(utilisateurRepository.findByEmail("client@mail.com")).thenReturn(Optional.of(client));
        when(opportuniteService.trouverParId(10L)).thenReturn(opp);

        ExtendedModelMap model = new ExtendedModelMap();
        String view = controller.formulaireModification(10L, userDetails, model);

        assertEquals("client/opportunite-form", view);
        assertTrue(model.containsAttribute("opportuniteRequest"));
        assertTrue(model.containsAttribute("opportuniteId"));
    }

    @Test
    void formulaireModificationShouldRedirectWhenClientIsNotVendeur() {
        Utilisateur client = utilisateur(1L, "client@mail.com");
        Utilisateur autreVendeur = utilisateur(2L, "autre@mail.com");
        UserDetails userDetails = user("client@mail.com");
        Opportunite opp = opportunite(10L, autreVendeur);

        when(utilisateurRepository.findByEmail("client@mail.com")).thenReturn(Optional.of(client));
        when(opportuniteService.trouverParId(10L)).thenReturn(opp);

        String view = controller.formulaireModification(10L, userDetails, new ExtendedModelMap());

        assertEquals("redirect:/client/dashboard", view);
    }

    @Test
    void modifierOpportuniteShouldRedirectToDashboardOnSuccess() {
        Utilisateur client = utilisateur(1L, "client@mail.com");
        UserDetails userDetails = user("client@mail.com");
        when(utilisateurRepository.findByEmail("client@mail.com")).thenReturn(Optional.of(client));

        CreerOpportuniteMarketplaceRequest req = request("Nouveau titre", "Nouvelle desc", BigDecimal.ONE);
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(req, "opportuniteRequest");
        RedirectAttributesModelMap redirectAttrs = new RedirectAttributesModelMap();

        String view = controller.modifierOpportunite(10L, req, bindingResult, userDetails, redirectAttrs);

        assertEquals("redirect:/client/dashboard", view);
        verify(opportuniteService).modifier(10L, req, client);
    }

    @Test
    void supprimerOpportuniteShouldDeleteAndRedirect() {
        Utilisateur client = utilisateur(1L, "client@mail.com");
        UserDetails userDetails = user("client@mail.com");
        when(utilisateurRepository.findByEmail("client@mail.com")).thenReturn(Optional.of(client));

        RedirectAttributesModelMap redirectAttrs = new RedirectAttributesModelMap();
        String view = controller.supprimerOpportunite(10L, userDetails, redirectAttrs);

        assertEquals("redirect:/client/dashboard", view);
        verify(opportuniteService).supprimer(10L, client);
    }

    @Test
    void voirDetailOpportuniteShouldAddOpportuniteAndFlagsToModel() {
        Utilisateur client = utilisateur(1L, "client@mail.com");
        UserDetails userDetails = user("client@mail.com");
        Opportunite opp = opportunite(10L, utilisateur(2L, "autre@mail.com"));

        when(utilisateurRepository.findByEmail("client@mail.com")).thenReturn(Optional.of(client));
        when(opportuniteService.trouverParId(10L)).thenReturn(opp);
        when(demandeService.listerParClient(client)).thenReturn(List.of());

        ExtendedModelMap model = new ExtendedModelMap();
        String view = controller.voirDetailOpportunite(10L, userDetails, model);

        assertEquals("client/opportunite-detail", view);
        assertTrue(model.containsAttribute("opportunite"));
        assertTrue(model.containsAttribute("estLeVendeur"));
        assertTrue(model.containsAttribute("dejaDemande"));
        assertEquals(false, model.getAttribute("estLeVendeur"));
    }

    @Test
    void creerDemandeShouldRedirectToDashboardOnSuccess() {
        Utilisateur client = utilisateur(1L, "client@mail.com");
        UserDetails userDetails = user("client@mail.com");
        when(utilisateurRepository.findByEmail("client@mail.com")).thenReturn(Optional.of(client));

        RedirectAttributesModelMap redirectAttrs = new RedirectAttributesModelMap();
        String view = controller.creerDemande(10L, userDetails, redirectAttrs);

        assertEquals("redirect:/client/dashboard", view);
        verify(demandeService).creerDemande(10L, client);
    }

    @Test
    void accepterAffectationShouldRedirectToDashboard() {
        Utilisateur client = utilisateur(1L, "client@mail.com");
        UserDetails userDetails = user("client@mail.com");
        when(utilisateurRepository.findByEmail("client@mail.com")).thenReturn(Optional.of(client));

        RedirectAttributesModelMap redirectAttrs = new RedirectAttributesModelMap();
        String view = controller.accepterAffectation(50L, userDetails, redirectAttrs);

        assertEquals("redirect:/client/dashboard", view);
        verify(affectationService).accepterAffectation(50L, client);
    }

    @Test
    void refuserAffectationShouldRedirectToDashboard() {
        Utilisateur client = utilisateur(1L, "client@mail.com");
        UserDetails userDetails = user("client@mail.com");
        when(utilisateurRepository.findByEmail("client@mail.com")).thenReturn(Optional.of(client));

        RedirectAttributesModelMap redirectAttrs = new RedirectAttributesModelMap();
        String view = controller.refuserAffectation(50L, userDetails, redirectAttrs);

        assertEquals("redirect:/client/dashboard", view);
        verify(affectationService).refuserAffectation(50L, client);
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private Utilisateur utilisateur(Long id, String email) {
        Utilisateur u = new Utilisateur();
        u.setId(id);
        u.setRole(Role.CLIENT);
        u.setEmail(email);
        u.setNom("User " + id);
        return u;
    }

    private Opportunite opportunite(Long id, Utilisateur vendeur) {
        Opportunite o = new Opportunite();
        o.setId(id);
        o.setStatut(StatutOpportunite.DISPONIBLE);
        o.setClientVendeur(vendeur);
        o.setTitre("Titre");
        o.setDescriptionComplete("Desc");
        o.setMontant(BigDecimal.TEN);
        return o;
    }

    private CreerOpportuniteMarketplaceRequest request(String titre, String desc, BigDecimal montant) {
        CreerOpportuniteMarketplaceRequest r = new CreerOpportuniteMarketplaceRequest();
        r.setTitre(titre);
        r.setDescriptionComplete(desc);
        r.setMontant(montant);
        return r;
    }

    private UserDetails user(String email) {
        return User.withUsername(email).password("pw").roles("CLIENT").build();
    }
}
