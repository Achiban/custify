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
    public Client saveClient(Client client, Utilisateur user) {

        if (clientRepository.existsByEmail(client.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        client.setUtilisateur(user);
        return clientRepository.save(client);
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
}