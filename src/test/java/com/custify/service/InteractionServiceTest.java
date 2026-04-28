package com.custify.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.custify.dto.CreerInteractionRequest;
import com.custify.exception.ClientNonTrouveException;
import com.custify.model.Client;
import com.custify.model.Interaction;
import com.custify.model.Utilisateur;
import com.custify.model.enums.Role;
import com.custify.model.enums.TypeInteraction;
import com.custify.repository.ClientRepository;
import com.custify.repository.InteractionRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InteractionServiceTest {

    @Mock
    private InteractionRepository interactionRepository;

    @Mock
    private ClientRepository clientRepository;

    private InteractionService interactionService;

    private Utilisateur commercial;
    private Utilisateur autreCommercial;
    private Utilisateur admin;
    private Client clientCommercial;
    private Client clientAutre;

    @BeforeEach
    void setUp() {
        interactionService = new InteractionService();
        org.springframework.test.util.ReflectionTestUtils.setField(interactionService, "interactionRepository", interactionRepository);
        org.springframework.test.util.ReflectionTestUtils.setField(interactionService, "clientRepository", clientRepository);

        commercial = buildUser(1L, Role.COMMERCIAL);
        autreCommercial = buildUser(2L, Role.COMMERCIAL);
        admin = buildUser(3L, Role.ADMIN);

        clientCommercial = buildClient(10L, commercial);
        clientAutre = buildClient(11L, autreCommercial);
    }

    @Test
    void createInteractionShouldPersistWhenCommercialOwnsClient() {
        CreerInteractionRequest request = buildRequest(10L, TypeInteraction.APPEL, "Appel de suivi");
        when(clientRepository.findById(10L)).thenReturn(Optional.of(clientCommercial));
        when(interactionRepository.save(any(Interaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Interaction result = interactionService.createInteraction(request, commercial);

        assertEquals(TypeInteraction.APPEL, result.getType());
        assertEquals("Appel de suivi", result.getResume());
        assertEquals(clientCommercial, result.getClient());
        assertEquals(commercial, result.getUtilisateur());

        ArgumentCaptor<Interaction> captor = ArgumentCaptor.forClass(Interaction.class);
        verify(interactionRepository).save(captor.capture());
        assertEquals(clientCommercial, captor.getValue().getClient());
        assertEquals(commercial, captor.getValue().getUtilisateur());
    }

    @Test
    void createInteractionShouldAllowAdminForAnyClient() {
        CreerInteractionRequest request = buildRequest(11L, TypeInteraction.EMAIL, "Email de relance");
        when(clientRepository.findById(11L)).thenReturn(Optional.of(clientAutre));
        when(interactionRepository.save(any(Interaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Interaction result = interactionService.createInteraction(request, admin);

        assertEquals(TypeInteraction.EMAIL, result.getType());
        assertEquals(clientAutre, result.getClient());
        assertEquals(admin, result.getUtilisateur());
    }

    @Test
    void createInteractionShouldThrowWhenClientDoesNotExist() {
        CreerInteractionRequest request = buildRequest(99L, TypeInteraction.APPEL, "Test");
        when(clientRepository.findById(99L)).thenReturn(Optional.empty());

        ClientNonTrouveException exception = assertThrows(
                ClientNonTrouveException.class,
                () -> interactionService.createInteraction(request, commercial));

        assertTrue(exception.getMessage().contains("99"));
        verify(interactionRepository, never()).save(any(Interaction.class));
    }

    @Test
    void createInteractionShouldThrowWhenCommercialDoesNotOwnClient() {
        CreerInteractionRequest request = buildRequest(11L, TypeInteraction.APPEL, "Test");
        when(clientRepository.findById(11L)).thenReturn(Optional.of(clientAutre));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> interactionService.createInteraction(request, commercial));

        assertTrue(exception.getMessage().contains("permission"));
        verify(interactionRepository, never()).save(any(Interaction.class));
    }

    @Test
    void getInteractionsByUserShouldReturnAllForAdmin() {
        List<Interaction> attendues = List.of(new Interaction(), new Interaction());
        when(interactionRepository.findAll()).thenReturn(attendues);

        List<Interaction> resultats = interactionService.getInteractionsByUser(admin);

        assertEquals(attendues, resultats);
        verify(interactionRepository).findAll();
    }

    @Test
    void getInteractionsByUserShouldFilterByOwnerForCommercial() {
        List<Interaction> attendues = List.of(new Interaction());
        when(interactionRepository.findByUtilisateurId(1L)).thenReturn(attendues);

        List<Interaction> resultats = interactionService.getInteractionsByUser(commercial);

        assertEquals(attendues, resultats);
        verify(interactionRepository).findByUtilisateurId(1L);
    }

    @Test
    void getInteractionsByClientShouldDelegateToRepository() {
        List<Interaction> attendues = List.of(new Interaction());
        when(interactionRepository.findByClientId(10L)).thenReturn(attendues);

        List<Interaction> resultats = interactionService.getInteractionsByClient(10L);

        assertEquals(attendues, resultats);
        verify(interactionRepository).findByClientId(10L);
    }

    @Test
    void getInteractionByIdShouldThrowWhenMissing() {
        when(interactionRepository.findById(100L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> interactionService.getInteractionById(100L));

        assertTrue(exception.getMessage().contains("100"));
    }

    @Test
    void updateInteractionShouldUpdateOwnedInteraction() {
        Interaction interaction = buildInteraction(20L, commercial, clientCommercial);
        CreerInteractionRequest request = buildRequest(10L, TypeInteraction.EMAIL, "Email mis a jour");
        when(interactionRepository.findById(20L)).thenReturn(Optional.of(interaction));
        when(interactionRepository.save(any(Interaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Interaction result = interactionService.updateInteraction(20L, request, commercial);

        assertEquals(TypeInteraction.EMAIL, result.getType());
        assertEquals("Email mis a jour", result.getResume());
        verify(interactionRepository).save(interaction);
    }

    @Test
    void updateInteractionShouldThrowWhenCommercialDoesNotOwnInteraction() {
        Interaction interaction = buildInteraction(21L, autreCommercial, clientAutre);
        CreerInteractionRequest request = buildRequest(11L, TypeInteraction.APPEL, "Test");
        when(interactionRepository.findById(21L)).thenReturn(Optional.of(interaction));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> interactionService.updateInteraction(21L, request, commercial));

        assertTrue(exception.getMessage().contains("permission"));
        verify(interactionRepository, never()).save(any(Interaction.class));
    }

    @Test
    void deleteInteractionShouldDeleteOwnedInteraction() {
        Interaction interaction = buildInteraction(30L, commercial, clientCommercial);
        when(interactionRepository.findById(30L)).thenReturn(Optional.of(interaction));

        interactionService.deleteInteraction(30L, commercial);

        verify(interactionRepository).deleteById(30L);
    }

    @Test
    void deleteInteractionShouldThrowWhenCommercialDoesNotOwnInteraction() {
        Interaction interaction = buildInteraction(31L, autreCommercial, clientAutre);
        when(interactionRepository.findById(31L)).thenReturn(Optional.of(interaction));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> interactionService.deleteInteraction(31L, commercial));

        assertTrue(exception.getMessage().contains("permission"));
        verify(interactionRepository, never()).deleteById(31L);
    }

    private Utilisateur buildUser(Long id, Role role) {
        Utilisateur user = new Utilisateur();
        user.setId(id);
        user.setNom("User " + id);
        user.setEmail("user" + id + "@mail.com");
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
        interaction.setResume("Resume");
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