package com.custify.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.custify.dto.CreerUtilisateurRequest;
import com.custify.dto.ModifierRoleRequest;
import com.custify.dto.ModifierUtilisateurRequest;
import com.custify.dto.UtilisateurResponse;
import com.custify.model.enums.Role;
import com.custify.service.UtilisateurService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

@ExtendWith(MockitoExtension.class)
class UtilisateurControllerTest {

    @Mock
    private UtilisateurService utilisateurService;

    private UtilisateurController utilisateurController;

    @BeforeEach
    void setUp() {
        utilisateurController = new UtilisateurController(utilisateurService);
    }

    @Test
    void listerShouldAddUsersToModel() {
        var utilisateurs = java.util.List.of(
                new UtilisateurResponse(1L, "Alice", "alice@mail.com", Role.ADMIN));
        when(utilisateurService.listerTous()).thenReturn(utilisateurs);
        ExtendedModelMap model = new ExtendedModelMap();

        String viewName = utilisateurController.lister(model);

        assertEquals("users/list", viewName);
        assertEquals(utilisateurs, model.getAttribute("utilisateurs"));
    }

    @Test
    void afficherFormulaireCreationShouldInitializeForm() {
        ExtendedModelMap model = new ExtendedModelMap();

        String viewName = utilisateurController.afficherFormulaireCreation(model);

        assertEquals("users/form", viewName);
        assertTrue(model.getAttribute("utilisateur") instanceof CreerUtilisateurRequest);
    }

    @Test
    void creerShouldReturnFormWhenBindingHasErrors() {
        CreerUtilisateurRequest request = new CreerUtilisateurRequest();
        BindingResult bindingResult = new BeanPropertyBindingResult(request, "utilisateur");
        bindingResult.rejectValue("nom", "NotBlank", "Le nom est obligatoire");
        RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();

        String viewName = utilisateurController.creer(request, bindingResult, redirectAttributes);

        assertEquals("users/form", viewName);
    }

    @Test
    void creerShouldRedirectAndAddFlashMessageWhenValid() {
        CreerUtilisateurRequest request = new CreerUtilisateurRequest();
        request.setNom("Alice");
        request.setEmail("alice@mail.com");
        request.setMotDePasse("password123");
        request.setRole(Role.ADMIN);
        BindingResult bindingResult = new BeanPropertyBindingResult(request, "utilisateur");
        RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();
        when(utilisateurService.creer(any(CreerUtilisateurRequest.class)))
                .thenReturn(new UtilisateurResponse(7L, "Alice", "alice@mail.com", Role.ADMIN));

        String viewName = utilisateurController.creer(request, bindingResult, redirectAttributes);

        assertEquals("redirect:/users", viewName);
        assertEquals("Utilisateur créé avec succès avec l'ID: 7", redirectAttributes.getFlashAttributes().get("message"));
        verify(utilisateurService).creer(request);
    }

    @Test
    void afficherFormulaireModificationRoleShouldPopulateModel() {
        UtilisateurResponse utilisateur = new UtilisateurResponse(3L, "Bob", "bob@mail.com", Role.COMMERCIAL);
        when(utilisateurService.trouverParId(3L)).thenReturn(utilisateur);
        ExtendedModelMap model = new ExtendedModelMap();

        String viewName = utilisateurController.afficherFormulaireModificationRole(3L, model);

        assertEquals("users/edit-role", viewName);
        assertEquals(utilisateur, model.getAttribute("utilisateur"));
        ModifierRoleRequest request = (ModifierRoleRequest) model.getAttribute("modifierRoleRequest");
        assertEquals(Role.COMMERCIAL, request.getRole());
    }

    @Test
    void modifierRoleShouldReturnFormWhenBindingHasErrors() {
        ModifierRoleRequest request = new ModifierRoleRequest();
        BindingResult bindingResult = new BeanPropertyBindingResult(request, "modifierRoleRequest");
        bindingResult.rejectValue("role", "NotNull", "Le role est obligatoire");
        ExtendedModelMap model = new ExtendedModelMap();
        when(utilisateurService.trouverParId(4L)).thenReturn(new UtilisateurResponse(4L, "Eve", "eve@mail.com", Role.ADMIN));
        RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();

        String viewName = utilisateurController.modifierRole(4L, request, bindingResult, model, redirectAttributes);

        assertEquals("users/edit-role", viewName);
        assertEquals("Eve", ((UtilisateurResponse) model.getAttribute("utilisateur")).getNom());
    }

    @Test
    void modifierRoleShouldRedirectWhenValid() {
        ModifierRoleRequest request = new ModifierRoleRequest();
        request.setRole(Role.ADMIN);
        BindingResult bindingResult = new BeanPropertyBindingResult(request, "modifierRoleRequest");
        ExtendedModelMap model = new ExtendedModelMap();
        RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();

        String viewName = utilisateurController.modifierRole(5L, request, bindingResult, model, redirectAttributes);

        assertEquals("redirect:/users", viewName);
        assertEquals("Rôle modifié avec succès", redirectAttributes.getFlashAttributes().get("message"));
        verify(utilisateurService).modifierRole(5L, Role.ADMIN);
    }

    @Test
    void afficherFormulaireModificationShouldPopulateModel() {
        UtilisateurResponse utilisateur = new UtilisateurResponse(6L, "Claire", "claire@mail.com", Role.ADMIN);
        when(utilisateurService.trouverParId(6L)).thenReturn(utilisateur);
        ExtendedModelMap model = new ExtendedModelMap();

        String viewName = utilisateurController.afficherFormulaireModification(6L, model);

        assertEquals("users/edit", viewName);
        assertEquals(utilisateur, model.getAttribute("utilisateur"));
        ModifierUtilisateurRequest request = (ModifierUtilisateurRequest) model.getAttribute("modifierUtilisateurRequest");
        assertEquals("Claire", request.getNom());
        assertEquals("claire@mail.com", request.getEmail());
    }

    @Test
    void modifierShouldReturnFormWhenBindingHasErrors() {
        ModifierUtilisateurRequest request = new ModifierUtilisateurRequest();
        BindingResult bindingResult = new BeanPropertyBindingResult(request, "modifierUtilisateurRequest");
        bindingResult.rejectValue("nom", "NotBlank", "Le nom est obligatoire");
        ExtendedModelMap model = new ExtendedModelMap();
        when(utilisateurService.trouverParId(8L)).thenReturn(new UtilisateurResponse(8L, "Denis", "denis@mail.com", Role.COMMERCIAL));
        RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();

        String viewName = utilisateurController.modifier(8L, request, bindingResult, model, redirectAttributes);

        assertEquals("users/edit", viewName);
        assertEquals("Denis", ((UtilisateurResponse) model.getAttribute("utilisateur")).getNom());
    }

    @Test
    void modifierShouldRedirectWhenValid() {
        ModifierUtilisateurRequest request = new ModifierUtilisateurRequest();
        request.setNom("Denis");
        request.setEmail("denis@mail.com");
        BindingResult bindingResult = new BeanPropertyBindingResult(request, "modifierUtilisateurRequest");
        ExtendedModelMap model = new ExtendedModelMap();
        RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();

        String viewName = utilisateurController.modifier(9L, request, bindingResult, model, redirectAttributes);

        assertEquals("redirect:/users", viewName);
        assertEquals("Utilisateur modifié avec succès", redirectAttributes.getFlashAttributes().get("message"));
        verify(utilisateurService).modifier(9L, request);
    }

    @Test
    void supprimerShouldRedirectWithMessage() {
        RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();

        String viewName = utilisateurController.supprimer(10L, redirectAttributes);

        assertEquals("redirect:/users", viewName);
        assertEquals("Utilisateur supprimé avec succès", redirectAttributes.getFlashAttributes().get("message"));
        verify(utilisateurService).supprimer(10L);
    }
}
