package com.custify.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.custify.dto.CreerInteractionRequest;
import com.custify.exception.ClientNonTrouveException;
import com.custify.model.Client;
import com.custify.model.Interaction;
import com.custify.model.Utilisateur;
import com.custify.model.enums.Role;
import com.custify.model.enums.TypeInteraction;
import com.custify.repository.UtilisateurRepository;
import com.custify.service.ClientService;
import com.custify.service.InteractionService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

@ExtendWith(MockitoExtension.class)
class InteractionControllerTest {

    @Mock
    private InteractionService interactionService;

    @Mock
    private ClientService clientService;

    @Mock
    private UtilisateurRepository utilisateurRepository;

    @InjectMocks
    private InteractionController interactionController;

    private Utilisateur admin;
    private Utilisateur commercial;
    private Client clientCommercial;
    private Client clientAutre;

    @BeforeEach
    void setUp() {
        admin = buildUser(1L, Role.ADMIN, "admin@mail.com");
        commercial = buildUser(2L, Role.COMMERCIAL, "comm@mail.com");
        clientCommercial = buildClient(10L, commercial);
        clientAutre = buildClient(11L, buildUser(3L, Role.COMMERCIAL, "other@mail.com"));

        SecurityContextHolder.clearContext();
    }

    @Test
    void redirectToListShouldRedirectToListPage() {
        assertEquals("redirect:/interactions/list", interactionController.redirectToList());
    }

    @Test
    void listAllInteractionsShouldPopulateModelForAuthenticatedUser() {
        authenticateAs(commercial);
        List<Interaction> interactions = List.of(buildInteraction(20L, commercial, clientCommercial));
        when(utilisateurRepository.findByEmail("comm@mail.com")).thenReturn(Optional.of(commercial));
        when(interactionService.getInteractionsByUser(commercial)).thenReturn(interactions);

        ExtendedModelMap model = new ExtendedModelMap();
        String viewName = interactionController.listAllInteractions(model);

        assertEquals("interactions/global-list", viewName);
        assertEquals(interactions, model.getAttribute("interactions"));
        assertEquals(1, model.getAttribute("totalCount"));
    }

    @Test
    void showNewInteractionFormShouldExposeClientsForCommercial() {
        authenticateAs(commercial);
        when(utilisateurRepository.findByEmail("comm@mail.com")).thenReturn(Optional.of(commercial));
        when(clientService.getClientsByUser(commercial)).thenReturn(List.of(clientCommercial));

        ExtendedModelMap model = new ExtendedModelMap();
        String viewName = interactionController.showNewInteractionForm(model);

        assertEquals("interactions/form", viewName);
        assertEquals(List.of(clientCommercial), model.getAttribute("clients"));
        assertTrue(model.getAttribute("interaction") instanceof CreerInteractionRequest);
    }

    @Test
    void showNewInteractionFormWithClientShouldReturnAccessDeniedForUnauthorizedUser() {
        authenticateAs(commercial);
        when(utilisateurRepository.findByEmail("comm@mail.com")).thenReturn(Optional.of(commercial));
        when(clientService.getClientById(11L)).thenReturn(clientAutre);

        ExtendedModelMap model = new ExtendedModelMap();
        String viewName = interactionController.showNewInteractionFormWithClient(11L, model);

        assertEquals("access-denied", viewName);
        assertTrue(model.getAttribute("error").toString().contains("permission"));
    }

    @Test
    void showNewInteractionFormWithClientShouldPopulateModelForAdmin() {
        authenticateAs(admin);
        when(utilisateurRepository.findByEmail("admin@mail.com")).thenReturn(Optional.of(admin));
        when(clientService.getClientById(10L)).thenReturn(clientCommercial);
        when(clientService.getAllClients()).thenReturn(List.of(clientCommercial, clientAutre));

        ExtendedModelMap model = new ExtendedModelMap();
        String viewName = interactionController.showNewInteractionFormWithClient(10L, model);

        assertEquals("interactions/form", viewName);
        assertEquals(clientCommercial, model.getAttribute("client"));
        assertEquals(List.of(clientCommercial, clientAutre), model.getAttribute("clients"));
    }

    @Test
    void createInteractionShouldReturnFormWhenBindingHasErrors() {
        authenticateAs(commercial);

        CreerInteractionRequest request = buildRequest(10L, TypeInteraction.EMAIL, "Résumé");
        BindingResult bindingResult = new BeanPropertyBindingResult(request, "interaction");
        bindingResult.rejectValue("resume", "NotBlank", "Le résumé est obligatoire");

        ExtendedModelMap model = new ExtendedModelMap();
        RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();

        String viewName = interactionController.createInteraction(request, bindingResult, model, redirectAttributes);

        assertEquals("interactions/form", viewName);
        assertEquals(request, model.getAttribute("interaction"));
    }

    @Test
    void createInteractionShouldRedirectOnSuccess() {
        authenticateAs(commercial);
        when(utilisateurRepository.findByEmail("comm@mail.com")).thenReturn(Optional.of(commercial));

        CreerInteractionRequest request = buildRequest(10L, TypeInteraction.APPEL, "Appel de suivi");
        BindingResult bindingResult = new BeanPropertyBindingResult(request, "interaction");

        String viewName = interactionController.createInteraction(request, bindingResult, new ExtendedModelMap(), new RedirectAttributesModelMap());

        assertEquals("redirect:/interactions/list", viewName);
        verify(interactionService).createInteraction(request, commercial);
    }

    @Test
    void getInteractionsByClientShouldReturnAccessDeniedForUnauthorizedUser() {
        authenticateAs(commercial);
        when(utilisateurRepository.findByEmail("comm@mail.com")).thenReturn(Optional.of(commercial));
        when(clientService.getClientById(11L)).thenReturn(clientAutre);

        ExtendedModelMap model = new ExtendedModelMap();
        String viewName = interactionController.getInteractionsByClient(11L, model);

        assertEquals("access-denied", viewName);
        assertTrue(model.getAttribute("error").toString().contains("permission"));
    }

    @Test
    void getInteractionDetailsShouldPopulateModelForOwner() {
        authenticateAs(commercial);
        when(utilisateurRepository.findByEmail("comm@mail.com")).thenReturn(Optional.of(commercial));
        Interaction interaction = buildInteraction(30L, commercial, clientCommercial);
        when(interactionService.getInteractionById(30L)).thenReturn(interaction);

        ExtendedModelMap model = new ExtendedModelMap();
        String viewName = interactionController.getInteractionDetails(30L, model);

        assertEquals("interactions/details", viewName);
        assertEquals(interaction, model.getAttribute("interaction"));
    }

    @Test
    void showEditFormShouldPopulateModelForOwner() {
        authenticateAs(commercial);
        when(utilisateurRepository.findByEmail("comm@mail.com")).thenReturn(Optional.of(commercial));
        Interaction interaction = buildInteraction(31L, commercial, clientCommercial);
        when(interactionService.getInteractionById(31L)).thenReturn(interaction);

        ExtendedModelMap model = new ExtendedModelMap();
        String viewName = interactionController.showEditForm(31L, model);

        assertEquals("interactions/form", viewName);
        assertEquals(31L, model.getAttribute("id"));
        assertEquals(clientCommercial, model.getAttribute("client"));
    }

    @Test
    void updateInteractionShouldRedirectOnSuccess() {
        authenticateAs(commercial);
        when(utilisateurRepository.findByEmail("comm@mail.com")).thenReturn(Optional.of(commercial));

        CreerInteractionRequest request = buildRequest(10L, TypeInteraction.EMAIL, "Mis à jour");
        BindingResult bindingResult = new BeanPropertyBindingResult(request, "interaction");
        when(interactionService.updateInteraction(32L, request, commercial)).thenReturn(buildInteraction(32L, commercial, clientCommercial));

        String viewName = interactionController.updateInteraction(32L, request, bindingResult, new ExtendedModelMap(), new RedirectAttributesModelMap());

        assertEquals("redirect:/interactions/list", viewName);
        verify(interactionService).updateInteraction(32L, request, commercial);
    }

    @Test
    void deleteInteractionShouldRedirectWithSuccessMessage() {
        authenticateAs(commercial);
        when(utilisateurRepository.findByEmail("comm@mail.com")).thenReturn(Optional.of(commercial));
        when(interactionService.getInteractionById(40L)).thenReturn(buildInteraction(40L, commercial, clientCommercial));

        RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();
        String viewName = interactionController.deleteInteraction(40L, redirectAttributes);

        assertEquals("redirect:/interactions/list", viewName);
        assertEquals("Interaction supprimée avec succès", redirectAttributes.getFlashAttributes().get("success"));
        verify(interactionService).deleteInteraction(40L, commercial);
    }

    @Test
    void deleteInteractionShouldReturnErrorWhenServiceThrows() {
        authenticateAs(commercial);
        when(utilisateurRepository.findByEmail("comm@mail.com")).thenReturn(Optional.of(commercial));
        when(interactionService.getInteractionById(41L)).thenReturn(buildInteraction(41L, commercial, clientCommercial));
        doThrow(new RuntimeException("permission")).when(interactionService).deleteInteraction(41L, commercial);

        RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();
        String viewName = interactionController.deleteInteraction(41L, redirectAttributes);

        assertEquals("redirect:/interactions/list", viewName);
        assertTrue(redirectAttributes.getFlashAttributes().get("error").toString().contains("permission"));
    }

    private void authenticateAs(Utilisateur user) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user.getEmail(), "n/a"));
        lenient().when(utilisateurRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
    }

    private Utilisateur buildUser(Long id, Role role, String email) {
        Utilisateur user = new Utilisateur();
        user.setId(id);
        user.setNom("User " + id);
        user.setEmail(email);
        user.setMotDePasse("secret");
        user.setRole(role);
        return user;
    }

    private Client buildClient(Long id, Utilisateur owner) {
        Client client = new Client();
        client.setId(id);
        client.setNom("Client " + id);
        client.setEmail("client" + id + "@mail.com");
        client.setTelephone("060000000" + id);
        client.setUtilisateur(owner);
        return client;
    }

    private Interaction buildInteraction(Long id, Utilisateur owner, Client client) {
        Interaction interaction = new Interaction();
        interaction.setId(id);
        interaction.setType(TypeInteraction.APPEL);
        interaction.setDateHeure(LocalDateTime.now());
        interaction.setResume("Résumé");
        interaction.setUtilisateur(owner);
        interaction.setClient(client);
        return interaction;
    }

    private CreerInteractionRequest buildRequest(Long clientId, TypeInteraction type, String resume) {
        CreerInteractionRequest request = new CreerInteractionRequest();
        request.setClientId(clientId);
        request.setType(type);
        request.setDateHeure(LocalDateTime.now());
        request.setResume(resume);
        return request;
    }
}