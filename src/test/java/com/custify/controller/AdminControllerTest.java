package com.custify.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.custify.dto.CreerUtilisateurRequest;
import com.custify.dto.ModifierRoleRequest;
import com.custify.dto.UtilisateurResponse;
import com.custify.model.enums.Role;
import com.custify.repository.UtilisateurRepository;
import com.custify.service.UtilisateurService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

@ExtendWith(MockitoExtension.class)
class AdminControllerTest {

    @Mock private UtilisateurService utilisateurService;
    @Mock private UtilisateurRepository utilisateurRepository;

    private AdminController controller;

    @BeforeEach
    void setUp() {
        controller = new AdminController(utilisateurService, utilisateurRepository);
    }

    @Test
    void dashboardShouldAddStatsToModelAndReturnView() {
        when(utilisateurRepository.count()).thenReturn(10L);
        when(utilisateurRepository.findByRole(Role.CLIENT)).thenReturn(List.of());
        when(utilisateurRepository.findByRole(Role.COMMERCIAL)).thenReturn(List.of());

        ExtendedModelMap model = new ExtendedModelMap();
        String view = controller.dashboard(model);

        assertEquals("admin/dashboard", view);
        assertEquals(10L, model.getAttribute("totalUtilisateurs"));
    }

    @Test
    void listerInternesShouldFilterOutClientsAndReturnView() {
        UtilisateurResponse admin = response(1L, Role.ADMIN);
        UtilisateurResponse commercial = response(2L, Role.COMMERCIAL);
        UtilisateurResponse client = response(3L, Role.CLIENT);
        when(utilisateurService.listerTous()).thenReturn(List.of(admin, commercial, client));

        ExtendedModelMap model = new ExtendedModelMap();
        String view = controller.listerInternes(model);

        assertEquals("users/list", view);
        List<?> utilisateurs = (List<?>) model.getAttribute("utilisateurs");
        assertEquals(2, utilisateurs.size());
    }

    @Test
    void listerClientsShouldFilterOnlyClientsAndReturnView() {
        UtilisateurResponse admin = response(1L, Role.ADMIN);
        UtilisateurResponse client = response(2L, Role.CLIENT);
        when(utilisateurService.listerTous()).thenReturn(List.of(admin, client));

        ExtendedModelMap model = new ExtendedModelMap();
        String view = controller.listerClients(model);

        assertEquals("users/clients", view);
        List<?> clients = (List<?>) model.getAttribute("clients");
        assertEquals(1, clients.size());
    }

    @Test
    void afficherFormulaireCreationShouldAddEmptyRequestToModel() {
        ExtendedModelMap model = new ExtendedModelMap();
        String view = controller.afficherFormulaireCreation(model);

        assertEquals("users/form", view);
        assertTrue(model.containsAttribute("utilisateur"));
    }

    @Test
    void creerShouldRedirectToUsersOnSuccess() {
        CreerUtilisateurRequest req = new CreerUtilisateurRequest();
        req.setNom("Alice");
        req.setEmail("alice@mail.com");
        req.setMotDePasse("pass");
        req.setRole(Role.COMMERCIAL);

        UtilisateurResponse created = response(1L, Role.COMMERCIAL);
        when(utilisateurService.creer(any())).thenReturn(created);

        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(req, "utilisateur");
        RedirectAttributesModelMap redirectAttrs = new RedirectAttributesModelMap();

        String view = controller.creer(req, bindingResult, redirectAttrs);

        assertEquals("redirect:/admin/users", view);
        assertTrue(redirectAttrs.getFlashAttributes().containsKey("message"));
    }

    @Test
    void creerShouldReturnFormViewOnValidationError() {
        CreerUtilisateurRequest req = new CreerUtilisateurRequest();
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(req, "utilisateur");
        bindingResult.rejectValue("nom", "required");

        String view = controller.creer(req, bindingResult, new RedirectAttributesModelMap());

        assertEquals("users/form", view);
    }

    @Test
    void afficherFormulaireRoleShouldAddUserAndRequestToModel() {
        UtilisateurResponse user = response(5L, Role.COMMERCIAL);
        when(utilisateurService.trouverParId(5L)).thenReturn(user);

        ExtendedModelMap model = new ExtendedModelMap();
        String view = controller.afficherFormulaireRole(5L, model);

        assertEquals("users/edit-role", view);
        assertTrue(model.containsAttribute("utilisateur"));
        assertTrue(model.containsAttribute("modifierRoleRequest"));
    }

    @Test
    void modifierRoleShouldRedirectToUsersOnSuccess() {
        ModifierRoleRequest req = new ModifierRoleRequest();
        req.setRole(Role.ADMIN);

        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(req, "modifierRoleRequest");
        RedirectAttributesModelMap redirectAttrs = new RedirectAttributesModelMap();

        String view = controller.modifierRole(5L, req, bindingResult, new ExtendedModelMap(), redirectAttrs);

        assertEquals("redirect:/admin/users", view);
        verify(utilisateurService).modifierRole(5L, Role.ADMIN);
    }

    @Test
    void supprimerShouldDeleteUserAndRedirect() {
        RedirectAttributesModelMap redirectAttrs = new RedirectAttributesModelMap();

        String view = controller.supprimer(7L, redirectAttrs);

        assertEquals("redirect:/admin/users", view);
        verify(utilisateurService).supprimer(7L);
        assertTrue(redirectAttrs.getFlashAttributes().containsKey("message"));
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private UtilisateurResponse response(Long id, Role role) {
        return new UtilisateurResponse(id, "User " + id, "user" + id + "@mail.com", role, "Entreprise SA");
    }
}
