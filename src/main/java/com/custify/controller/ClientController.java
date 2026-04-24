package com.custify.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.custify.model.Client;
import com.custify.model.Utilisateur;
import com.custify.repository.InteractionRepository;
import com.custify.repository.UtilisateurRepository;
import com.custify.service.ClientService;
import java.util.List;

@Controller
@RequestMapping("/clients")
public class ClientController {

    @Autowired
    private ClientService clientService;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private InteractionRepository interactionRepository;

    // DRECtion via list
    @GetMapping("")
    public String indexCliens(Model model) {
        return "redirect:/clients/list";
    }

    // GET FORM (US-04)
    @GetMapping("/new")
    public String showForm(Model model) {
        model.addAttribute("client", new Client());
        return "clients/form";
    }

    // SAVE CLIENT (US-04)
    @PostMapping("/save")
    public String saveClient(@ModelAttribute Client client) {

        Utilisateur user = getLoggedUser();
        clientService.saveClient(client, user);

        return "clients/list";
    }

    // LIST CLIENTS (US-04 + US-05) with search and filter
    @GetMapping("/list")
    public String listClients(
            Model model,
            String search,
            String filterNom,
            String filterEmail,
            String filterEntreprise,
            String filterTelephone) {

        Utilisateur user = getLoggedUser();
        
        List<Client> clients;
        
        // Apply filters based on parameters
        if (search != null && !search.trim().isEmpty()) {
            // Global search
            clients = clientService.searchClients(user, search);
            model.addAttribute("searchTerm", search);
        } else if (filterNom != null && !filterNom.trim().isEmpty()) {
            clients = clientService.filterByNom(user, filterNom);
            model.addAttribute("filterNom", filterNom);
        } else if (filterEmail != null && !filterEmail.trim().isEmpty()) {
            clients = clientService.filterByEmail(user, filterEmail);
            model.addAttribute("filterEmail", filterEmail);
        } else if (filterEntreprise != null && !filterEntreprise.trim().isEmpty()) {
            clients = clientService.filterByEntreprise(user, filterEntreprise);
            model.addAttribute("filterEntreprise", filterEntreprise);
        } else if (filterTelephone != null && !filterTelephone.trim().isEmpty()) {
            clients = clientService.filterByTelephone(user, filterTelephone);
            model.addAttribute("filterTelephone", filterTelephone);
        } else {
            // No filter applied
            clients = clientService.getClientsByUser(user);
        }
        
        model.addAttribute("clients", clients);

        return "clients/list";
    }

    // DETAILS (US-05)
    @GetMapping("/details/{id}")
    public String details(@PathVariable Long id, Model model) {

        Client client = clientService.getClientById(id);

        model.addAttribute("client", client);
        model.addAttribute("interactions",
                interactionRepository.findByClientId(id));

        return "clients/details";
    }

    // EDIT FORM
    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id, Model model) {

        model.addAttribute("client", clientService.getClientById(id));

        return "clients/edit";
    }

    // GET LOGGED USER
    private Utilisateur getLoggedUser() {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        return utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}