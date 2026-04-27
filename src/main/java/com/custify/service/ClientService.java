package com.custify.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.custify.exception.AccesNonAutoriseException;
import com.custify.exception.ClientNonTrouveException;
import com.custify.exception.DonneeDupliqueeException;
import com.custify.model.Client;
import com.custify.model.Utilisateur;
import com.custify.model.enums.Role;
import com.custify.repository.ClientRepository;

@Service
public class ClientService {

    @Autowired
    private ClientRepository clientRepository;

    // CREATE
    public void saveClient(Client client, Utilisateur user) {

        if (clientRepository.existsByEmail(client.getEmail())) {
            throw new DonneeDupliqueeException("Un client avec cet email existe deja.");
        }

        if (clientRepository.existsByTelephone(client.getTelephone())) {
            throw new DonneeDupliqueeException("Un client avec ce telephone existe deja.");
        }

        client.setUtilisateur(user);
        clientRepository.save(client);
    }

    // LIST by user
    public List<Client> getClientsByUser(Utilisateur user) {
        return clientRepository.findByUtilisateurId(user.getId());
    }

    // LIST all clients (Admin only)
    public List<Client> getAllClients() {
        return clientRepository.findAll();
    }

    // GET ONE
    public Client getClientById(Long id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new ClientNonTrouveException(id));
    }

    // GET ONE avec controle de propriete (Admin acces total, Commercial limite a ses fiches)
    public Client getClientForUser(Long id, Utilisateur user) {
        Client client = getClientById(id);
        verifierProprietaire(client, user);
        return client;
    }

    // UPDATE (US-05B)
    @Transactional
    public Client updateClient(Long id, Client donneesModifiees, Utilisateur user) {

        Client client = getClientForUser(id, user);

        String nouvelEmail = donneesModifiees.getEmail();
        String nouveauTelephone = donneesModifiees.getTelephone();

        if (clientRepository.existsByEmailAndIdNot(nouvelEmail, id)) {
            throw new DonneeDupliqueeException("Un autre client utilise deja cet email.");
        }

        if (clientRepository.existsByTelephoneAndIdNot(nouveauTelephone, id)) {
            throw new DonneeDupliqueeException("Un autre client utilise deja ce telephone.");
        }

        client.setNom(donneesModifiees.getNom());
        client.setEmail(nouvelEmail);
        client.setTelephone(nouveauTelephone);
        client.setEntreprise(donneesModifiees.getEntreprise());

        return clientRepository.save(client);
    }

    // DELETE (US-05C)
    @Transactional
    public void deleteClient(Long id, Utilisateur user) {
        Client client = getClientForUser(id, user);
        clientRepository.delete(client);
    }

    // SEARCH - Global search across all fields
    public List<Client> searchClients(Utilisateur user, String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getClientsByUser(user);
        }
        return clientRepository.searchClients(user.getId(), searchTerm.trim());
    }

    // FILTER - By name
    public List<Client> filterByNom(Utilisateur user, String nom) {
        if (nom == null || nom.trim().isEmpty()) {
            return getClientsByUser(user);
        }
        return clientRepository.findByUtilisateurIdAndNomContainingIgnoreCase(user.getId(), nom.trim());
    }

    // FILTER - By email
    public List<Client> filterByEmail(Utilisateur user, String email) {
        if (email == null || email.trim().isEmpty()) {
            return getClientsByUser(user);
        }
        return clientRepository.findByUtilisateurIdAndEmailContainingIgnoreCase(user.getId(), email.trim());
    }

    // FILTER - By company
    public List<Client> filterByEntreprise(Utilisateur user, String entreprise) {
        if (entreprise == null || entreprise.trim().isEmpty()) {
            return getClientsByUser(user);
        }
        return clientRepository.findByUtilisateurIdAndEntrepriseContainingIgnoreCase(user.getId(), entreprise.trim());
    }

    // FILTER - By phone
    public List<Client> filterByTelephone(Utilisateur user, String telephone) {
        if (telephone == null || telephone.trim().isEmpty()) {
            return getClientsByUser(user);
        }
        return clientRepository.findByUtilisateurIdAndTelephoneContainingIgnoreCase(user.getId(), telephone.trim());
    }

    private void verifierProprietaire(Client client, Utilisateur user) {
        if (user.getRole() == Role.ADMIN) {
            return;
        }
        if (!client.getUtilisateur().getId().equals(user.getId())) {
            throw new AccesNonAutoriseException();
        }
    }
}
