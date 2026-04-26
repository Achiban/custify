package com.custify.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.custify.exception.ClientNonTrouveException;
import com.custify.exception.DonneeDupliqueeException;
import com.custify.model.Client;
import com.custify.model.Utilisateur;
import com.custify.model.enums.Role;
import com.custify.repository.InteractionRepository;
import com.custify.repository.UtilisateurRepository;
import com.custify.service.ClientService;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

@ExtendWith(MockitoExtension.class)
class ClientControllerTest {

    @Mock
    private ClientService clientService;

    @Mock
    private UtilisateurRepository utilisateurRepository;

    @Mock
    private InteractionRepository interactionRepository;

    private ClientController clientController;
    private Utilisateur loggedUser;

    @BeforeEach
    void setUp() {
        clientController = new ClientController();
        ReflectionTestUtils.setField(clientController, "clientService", clientService);
        ReflectionTestUtils.setField(clientController, "utilisateurRepository", utilisateurRepository);
        ReflectionTestUtils.setField(clientController, "interactionRepository", interactionRepository);

        loggedUser = new Utilisateur();
        loggedUser.setId(1L);
        loggedUser.setNom("Commercial");
        loggedUser.setEmail("commercial@mail.com");
        loggedUser.setRole(Role.COMMERCIAL);

        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken("commercial@mail.com", "n/a"));
        lenient().when(utilisateurRepository.findByEmail("commercial@mail.com")).thenReturn(Optional.of(loggedUser));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void showFormShouldInitializeClientInModel() {
        ExtendedModelMap model = new ExtendedModelMap();

        String viewName = clientController.showForm(model);

        assertEquals("clients/form", viewName);
        assertTrue(model.getAttribute("client") instanceof Client);
    }

    @Test
    void saveClientShouldRedirectOnSuccess() {
        Client client = buildClient(10L, "Alice", "alice@mail.com", "0611223344", "ACME");
        ExtendedModelMap model = new ExtendedModelMap();

        String viewName = clientController.saveClient(client, model);

        assertEquals("redirect:/clients/list", viewName);
        verify(clientService).saveClient(client, loggedUser);
    }

    @Test
    void saveClientShouldReturnFormWhenDuplicateData() {
        Client client = buildClient(11L, "Alice", "alice@mail.com", "0611223344", "ACME");
        ExtendedModelMap model = new ExtendedModelMap();
        doThrow(new DonneeDupliqueeException("Un client avec cet email existe deja."))
            .when(clientService)
            .saveClient(client, loggedUser);

        String viewName = clientController.saveClient(client, model);

        assertEquals("clients/form", viewName);
        assertEquals("Un client avec cet email existe deja.", model.getAttribute("error"));
        assertEquals(client, model.getAttribute("client"));
    }

    @Test
    void listClientsShouldUseSearchWhenProvided() {
        ExtendedModelMap model = new ExtendedModelMap();
        List<Client> clients = List.of(buildClient(12L, "Bob", "bob@mail.com", "0622334455", "Beta"));
        when(clientService.searchClients(loggedUser, "bob")).thenReturn(clients);

        String viewName = clientController.listClients(model, "bob", null, null, null, null);

        assertEquals("clients/list", viewName);
        assertEquals("bob", model.getAttribute("searchTerm"));
        assertEquals(clients, model.getAttribute("clients"));
        verify(clientService).searchClients(loggedUser, "bob");
    }

    @Test
    void detailsShouldPopulateModelWhenClientExists() {
        Client client = buildClient(13L, "Clara", "clara@mail.com", "0633445566", "Gamma");
        ExtendedModelMap model = new ExtendedModelMap();
        RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();
        when(clientService.getClientForUser(13L, loggedUser)).thenReturn(client);
        when(interactionRepository.findByClientId(13L)).thenReturn(List.of());

        String viewName = clientController.details(13L, model, redirectAttributes);

        assertEquals("clients/details", viewName);
        assertEquals(client, model.getAttribute("client"));
        assertEquals(List.of(), model.getAttribute("interactions"));
    }

    @Test
    void detailsShouldRedirectWhenClientNotFound() {
        ExtendedModelMap model = new ExtendedModelMap();
        RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();
        when(clientService.getClientForUser(99L, loggedUser)).thenThrow(new ClientNonTrouveException(99L));

        String viewName = clientController.details(99L, model, redirectAttributes);

        assertEquals("redirect:/clients/list", viewName);
        assertTrue(redirectAttributes.getFlashAttributes().get("error").toString().contains("99"));
    }

    @Test
    void updateClientShouldReturnEditViewWhenDuplicateData() {
        Client client = buildClient(null, "Dina", "dina@mail.com", "0655667788", "Delta");
        ExtendedModelMap model = new ExtendedModelMap();
        RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();
        when(clientService.updateClient(15L, client, loggedUser))
                .thenThrow(new DonneeDupliqueeException("Un autre client utilise deja cet email."));

        String viewName = clientController.updateClient(15L, client, model, redirectAttributes);

        assertEquals("clients/edit", viewName);
        assertEquals(15L, ((Client) model.getAttribute("client")).getId());
        assertEquals("Un autre client utilise deja cet email.", model.getAttribute("error"));
    }

    @Test
    void deleteClientShouldRedirectWithSuccessMessage() {
        RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();

        String viewName = clientController.deleteClient(16L, redirectAttributes);

        assertEquals("redirect:/clients/list", viewName);
        assertEquals("Le client a ete supprime avec succes.", redirectAttributes.getFlashAttributes().get("success"));
        verify(clientService).deleteClient(16L, loggedUser);
    }

    private Client buildClient(Long id, String nom, String email, String telephone, String entreprise) {
        Client client = new Client();
        client.setId(id);
        client.setNom(nom);
        client.setEmail(email);
        client.setTelephone(telephone);
        client.setEntreprise(entreprise);
        return client;
    }
}