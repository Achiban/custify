package com.custify.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.custify.exception.AccesNonAutoriseException;
import com.custify.exception.ClientNonTrouveException;
import com.custify.exception.DonneeDupliqueeException;
import com.custify.model.Client;
import com.custify.model.Utilisateur;
import com.custify.model.enums.Role;
import com.custify.repository.ClientRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ClientServiceTest {

    @Mock
    private ClientRepository clientRepository;

    @InjectMocks
    private ClientService clientService;

    private Utilisateur commercial;
    private Utilisateur autreCommercial;
    private Utilisateur admin;

    @BeforeEach
    void setUp() {
        commercial = buildUser(1L, Role.COMMERCIAL);
        autreCommercial = buildUser(2L, Role.COMMERCIAL);
        admin = buildUser(3L, Role.ADMIN);
    }

    @Test
    void saveClientShouldThrowWhenEmailAlreadyExists() {
        Client client = buildClient(10L, "Alice", "alice@mail.com", "0611223344", "ACME", null);
        when(clientRepository.existsByEmail("alice@mail.com")).thenReturn(true);

        DonneeDupliqueeException exception = assertThrows(
                DonneeDupliqueeException.class,
                () -> clientService.saveClient(client, commercial));

        assertEquals("Un client avec cet email existe deja.", exception.getMessage());
        verify(clientRepository, never()).save(any(Client.class));
    }

    @Test
    void saveClientShouldThrowWhenTelephoneAlreadyExists() {
        Client client = buildClient(10L, "Alice", "alice@mail.com", "0611223344", "ACME", null);
        when(clientRepository.existsByEmail("alice@mail.com")).thenReturn(false);
        when(clientRepository.existsByTelephone("0611223344")).thenReturn(true);

        DonneeDupliqueeException exception = assertThrows(
                DonneeDupliqueeException.class,
                () -> clientService.saveClient(client, commercial));

        assertEquals("Un client avec ce telephone existe deja.", exception.getMessage());
        verify(clientRepository, never()).save(any(Client.class));
    }

    @Test
    void saveClientShouldSetOwnerAndPersist() {
        Client client = buildClient(10L, "Alice", "alice@mail.com", "0611223344", "ACME", null);
        when(clientRepository.existsByEmail("alice@mail.com")).thenReturn(false);
        when(clientRepository.existsByTelephone("0611223344")).thenReturn(false);
        when(clientRepository.save(any(Client.class))).thenAnswer(invocation -> invocation.getArgument(0));

        clientService.saveClient(client, commercial);

        ArgumentCaptor<Client> captor = ArgumentCaptor.forClass(Client.class);
        verify(clientRepository).save(captor.capture());
        assertEquals(commercial, captor.getValue().getUtilisateur());
    }

    @Test
    void getClientByIdShouldThrowWhenNotFound() {
        when(clientRepository.findById(99L)).thenReturn(Optional.empty());

        ClientNonTrouveException exception = assertThrows(
                ClientNonTrouveException.class,
                () -> clientService.getClientById(99L));

        assertTrue(exception.getMessage().contains("99"));
    }

    @Test
    void getClientForUserShouldReturnClientForOwner() {
        Client client = buildClient(20L, "Bob", "bob@mail.com", "0622334455", "Beta", commercial);
        when(clientRepository.findById(20L)).thenReturn(Optional.of(client));

        Client resultat = clientService.getClientForUser(20L, commercial);

        assertEquals(20L, resultat.getId());
    }

    @Test
    void getClientForUserShouldAllowAdminAccess() {
        Client client = buildClient(21L, "Bob", "bob@mail.com", "0622334455", "Beta", commercial);
        when(clientRepository.findById(21L)).thenReturn(Optional.of(client));

        Client resultat = clientService.getClientForUser(21L, admin);

        assertEquals(21L, resultat.getId());
    }

    @Test
    void getClientForUserShouldThrowWhenCommercialIsNotOwner() {
        Client client = buildClient(22L, "Bob", "bob@mail.com", "0622334455", "Beta", commercial);
        when(clientRepository.findById(22L)).thenReturn(Optional.of(client));

        assertThrows(AccesNonAutoriseException.class, () -> clientService.getClientForUser(22L, autreCommercial));
    }

    @Test
    void updateClientShouldThrowWhenAnotherClientUsesEmail() {
        Client existant = buildClient(30L, "Clara", "clara@mail.com", "0633445566", "Gamma", commercial);
        Client modifie = buildClient(null, "Clara N", "autre@mail.com", "0633445566", "Gamma+", null);
        when(clientRepository.findById(30L)).thenReturn(Optional.of(existant));
        when(clientRepository.existsByEmailAndIdNot("autre@mail.com", 30L)).thenReturn(true);

        DonneeDupliqueeException exception = assertThrows(
                DonneeDupliqueeException.class,
                () -> clientService.updateClient(30L, modifie, commercial));

        assertEquals("Un autre client utilise deja cet email.", exception.getMessage());
        verify(clientRepository, never()).save(any(Client.class));
    }

    @Test
    void updateClientShouldUpdateFieldsAndPersist() {
        Client existant = buildClient(31L, "Clara", "clara@mail.com", "0633445566", "Gamma", commercial);
        Client modifie = buildClient(null, "Clara N", "clara.new@mail.com", "0600000000", "Gamma+", null);
        when(clientRepository.findById(31L)).thenReturn(Optional.of(existant));
        when(clientRepository.existsByEmailAndIdNot("clara.new@mail.com", 31L)).thenReturn(false);
        when(clientRepository.existsByTelephoneAndIdNot("0600000000", 31L)).thenReturn(false);
        when(clientRepository.save(any(Client.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Client resultat = clientService.updateClient(31L, modifie, commercial);

        assertEquals("Clara N", resultat.getNom());
        assertEquals("clara.new@mail.com", resultat.getEmail());
        assertEquals("0600000000", resultat.getTelephone());
        assertEquals("Gamma+", resultat.getEntreprise());
        verify(clientRepository).save(existant);
    }

    @Test
    void deleteClientShouldDeleteWhenAuthorized() {
        Client client = buildClient(40L, "Dina", "dina@mail.com", "0655667788", "Delta", commercial);
        when(clientRepository.findById(40L)).thenReturn(Optional.of(client));

        clientService.deleteClient(40L, commercial);

        verify(clientRepository).delete(client);
    }

    @Test
    void searchClientsShouldReturnAllWhenSearchIsBlank() {
        List<Client> attendus = List.of(
                buildClient(50L, "Emma", "emma@mail.com", "0677889900", "Echo", commercial));
        when(clientRepository.findByUtilisateurId(1L)).thenReturn(attendus);

        List<Client> resultat = clientService.searchClients(commercial, "   ");

        assertEquals(attendus, resultat);
        verify(clientRepository, never()).searchClients(any(Long.class), any(String.class));
    }

    @Test
    void searchClientsShouldTrimAndDelegateRepositorySearch() {
        List<Client> attendus = List.of(
                buildClient(51L, "Fiona", "fiona@mail.com", "0688990011", "Foxtrot", commercial));
        when(clientRepository.searchClients(1L, "fiona")).thenReturn(attendus);

        List<Client> resultat = clientService.searchClients(commercial, "  fiona  ");

        assertEquals(attendus, resultat);
        verify(clientRepository).searchClients(1L, "fiona");
    }

    @Test
    void filterByNomShouldDelegateToRepositoryWithTrimmedValue() {
        List<Client> attendus = List.of(
                buildClient(52L, "Gabi", "gabi@mail.com", "0699001122", "Golf", commercial));
        when(clientRepository.findByUtilisateurIdAndNomContainingIgnoreCase(1L, "ga")).thenReturn(attendus);

        List<Client> resultat = clientService.filterByNom(commercial, "  ga ");

        assertEquals(attendus, resultat);
        verify(clientRepository).findByUtilisateurIdAndNomContainingIgnoreCase(eq(1L), eq("ga"));
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

    private Client buildClient(Long id, String nom, String email, String telephone, String entreprise, Utilisateur owner) {
        Client client = new Client();
        client.setId(id);
        client.setNom(nom);
        client.setEmail(email);
        client.setTelephone(telephone);
        client.setEntreprise(entreprise);
        client.setUtilisateur(owner);
        return client;
    }
}