package com.custify.controller;

import java.util.List;

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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.custify.exception.AccesNonAutoriseException;
import com.custify.exception.ClientNonTrouveException;
import com.custify.exception.DonneeDupliqueeException;
import com.custify.model.Client;
import com.custify.model.Utilisateur;
import com.custify.repository.InteractionRepository;
import com.custify.repository.UtilisateurRepository;
import com.custify.service.ClientService;

@Controller
@RequestMapping("/clients")
public class ClientController {

    @Autowired
    private ClientService clientService;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private InteractionRepository interactionRepository;

    @GetMapping("")
    public String indexCliens() {
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
    public String saveClient(@ModelAttribute Client client, Model model) {

        try {
            Utilisateur user = getLoggedUser();
            clientService.saveClient(client, user);

            return "redirect:/clients/list";

        } catch (DonneeDupliqueeException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("client", client);

            return "clients/form";
        }
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

        if (search != null && !search.trim().isEmpty()) {
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
            clients = clientService.getClientsByUser(user);
        }

        model.addAttribute("clients", clients);

        return "clients/list";
    }

    // DETAILS (US-05A)
    @GetMapping("/details/{id}")
    public String details(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {

        try {
            Client client = clientService.getClientForUser(id, getLoggedUser());

            model.addAttribute("client", client);
            model.addAttribute("interactions", interactionRepository.findByClientId(id));

            return "clients/details";

        } catch (ClientNonTrouveException | AccesNonAutoriseException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/clients/list";
        }
    }

    // EDIT FORM (US-05B)
    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {

        try {
            Client client = clientService.getClientForUser(id, getLoggedUser());
            model.addAttribute("client", client);
            return "clients/edit";

        } catch (ClientNonTrouveException | AccesNonAutoriseException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/clients/list";
        }
    }

    // UPDATE CLIENT (US-05B)
    @PostMapping("/update/{id}")
    public String updateClient(
            @PathVariable Long id,
            @ModelAttribute Client client,
            Model model,
            RedirectAttributes redirectAttributes) {

        try {
            clientService.updateClient(id, client, getLoggedUser());
            redirectAttributes.addFlashAttribute("success", "Le client a ete modifie avec succes.");
            return "redirect:/clients/details/" + id;

        } catch (DonneeDupliqueeException e) {
            // Reaffiche le formulaire avec les valeurs saisies pour permettre la correction
            client.setId(id);
            model.addAttribute("client", client);
            model.addAttribute("error", e.getMessage());
            return "clients/edit";

        } catch (ClientNonTrouveException | AccesNonAutoriseException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/clients/list";
        }
    }

    // DELETE CLIENT (US-05C)
    @PostMapping("/delete/{id}")
    public String deleteClient(@PathVariable Long id, RedirectAttributes redirectAttributes) {

        try {
            clientService.deleteClient(id, getLoggedUser());
            redirectAttributes.addFlashAttribute("success", "Le client a ete supprime avec succes.");

        } catch (ClientNonTrouveException | AccesNonAutoriseException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/clients/list";
    }

    private Utilisateur getLoggedUser() {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        return utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
