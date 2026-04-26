package com.custify.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.custify.model.Client;
import com.custify.model.Utilisateur;
import com.custify.repository.ClientRepository;

@Service
public class ClientService {

    @Autowired
    private ClientRepository clientRepository;

    // CREATE
    public void saveClient(Client client, Utilisateur user) {

        if (clientRepository.existsByEmail(client.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        if (clientRepository.existsByTelephone(client.getTelephone())) {
            throw new RuntimeException("Telephone already exists");
        }

        client.setUtilisateur(user);
        clientRepository.save(client);
    }

    // LIST by user
    public List<Client> getClientsByUser(Utilisateur user) {
        return clientRepository.findByUtilisateurId(user.getId());
    }

    // GET ONE
    public Client getClientById(Long id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Client not found"));
    }

    // UPDATE
    public Client updateClient(Client updated) {
        Client client = getClientById(updated.getId());

        client.setNom(updated.getNom());
        client.setEmail(updated.getEmail());
        client.setTelephone(updated.getTelephone());
        client.setEntreprise(updated.getEntreprise());

        return clientRepository.save(client);
    }

    // DELETE
    public void deleteClient(Long id) {
        clientRepository.deleteById(id);
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
}