package com.custify.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.custify.dto.CreerInteractionRequest;
import com.custify.model.Client;
import com.custify.model.Interaction;
import com.custify.model.Utilisateur;
import com.custify.model.enums.TypeInteraction;
import com.custify.repository.UtilisateurRepository;
import com.custify.service.ClientService;
import com.custify.service.InteractionService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/interactions")
public class InteractionController {

    @Autowired
    private InteractionService interactionService;

    @Autowired
    private ClientService clientService;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    /**
     * Redirection racine vers la liste
     */
    @GetMapping("")
    public String redirectToList() {
        return "redirect:/interactions/list";
    }

    /**
     * Liste de TOUS les interactions (page principale)
     */
    @GetMapping("/list")
    public String listAllInteractions(Model model) {
        try {
            Utilisateur userConnecte = getCurrentUser();
            List<Interaction> interactions = interactionService.getInteractionsByUser(userConnecte);
            
            model.addAttribute("interactions", interactions);
            model.addAttribute("totalCount", interactions.size());
            return "interactions/global-list";
        } catch (Exception e) {
            model.addAttribute("error", "Erreur: " + e.getMessage());
            return "interactions/global-list";
        }
    }

    /**
     * Affiche le formulaire de création d'une interaction (sans client spécifié)
     */
    @GetMapping("/nouveau")
    public String showNewInteractionForm(Model model) {
        try {
            Utilisateur userConnecte = getCurrentUser();
            List<Client> clients;
            
            // Admin voir tous les clients, Commercial voir ses propres clients
            if (userConnecte.getRole().name().equals("ADMIN")) {
                clients = clientService.getAllClients();
            } else {
                clients = clientService.getClientsByUser(userConnecte);
            }
            
            model.addAttribute("clients", clients);
            model.addAttribute("interaction", new CreerInteractionRequest());
            model.addAttribute("typesInteraction", TypeInteraction.values());
            return "interactions/form";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "access-denied";
        }
    }

    /**
     * Affiche le formulaire de création d'une interaction (avec client spécifié)
     */
    @GetMapping("/nouveau/{clientId}")
    public String showNewInteractionFormWithClient(@PathVariable Long clientId, Model model) {
        try {
            Client client = clientService.getClientById(clientId);
            
            // Vérifier les permissions
            Utilisateur userConnecte = getCurrentUser();
            if (!userConnecte.getRole().name().equals("ADMIN") && !client.getUtilisateur().getId().equals(userConnecte.getId())) {
                model.addAttribute("error", "Vous n'avez pas la permission d'accéder à ce client");
                return "access-denied";
            }

            List<Client> clients;
            if (userConnecte.getRole().name().equals("ADMIN")) {
                clients = clientService.getAllClients();
            } else {
                clients = clientService.getClientsByUser(userConnecte);
            }

            model.addAttribute("client", client);
            model.addAttribute("clients", clients);
            model.addAttribute("interaction", new CreerInteractionRequest());
            model.addAttribute("typesInteraction", TypeInteraction.values());
            return "interactions/form";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "access-denied";
        }
    }

    /**
     * Enregistre une nouvelle interaction
     */
    @PostMapping("/creer")
    public String createInteraction(@Valid @ModelAttribute("interaction") CreerInteractionRequest request,
                                    BindingResult bindingResult, 
                                    Model model,
                                    RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            try {
                Client client = clientService.getClientById(request.getClientId());
                model.addAttribute("client", client);
                model.addAttribute("interaction", request);
                model.addAttribute("typesInteraction", TypeInteraction.values());
            } catch (Exception e) {
                model.addAttribute("error", e.getMessage());
            }
            return "interactions/form";
        }

        try {
            Utilisateur userConnecte = getCurrentUser();
            interactionService.createInteraction(request, userConnecte);
            redirectAttributes.addFlashAttribute("success", "Interaction enregistrée avec succès");
            return "redirect:/interactions/list";
        } catch (Exception e) {
            try {
                Client client = clientService.getClientById(request.getClientId());
                model.addAttribute("client", client);
                model.addAttribute("interaction", request);
                model.addAttribute("typesInteraction", TypeInteraction.values());
            } catch (Exception ex) {
                model.addAttribute("error", ex.getMessage());
            }
            model.addAttribute("error", "Erreur lors de l'enregistrement: " + e.getMessage());
            return "interactions/form";
        }
    }

    /**
     * Affiche la liste des interactions d'un client
     */
    @GetMapping("/client/{clientId}")
    public String getInteractionsByClient(@PathVariable Long clientId, Model model) {
        try {
            Client client = clientService.getClientById(clientId);
            
            // Vérifier les permissions
            Utilisateur userConnecte = getCurrentUser();
            if (!userConnecte.getRole().name().equals("ADMIN") && !client.getUtilisateur().getId().equals(userConnecte.getId())) {
                model.addAttribute("error", "Vous n'avez pas la permission d'accéder à ce client");
                return "access-denied";
            }

            List<Interaction> interactions = interactionService.getInteractionsByClient(clientId);
            model.addAttribute("client", client);
            model.addAttribute("interactions", interactions);
            return "interactions/list";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "access-denied";
        }
    }

    /**
     * Affiche les détails d'une interaction
     */
    @GetMapping("/details/{id}")
    public String getInteractionDetails(@PathVariable Long id, Model model) {
        try {
            Interaction interaction = interactionService.getInteractionById(id);
            
            // Vérifier les permissions
            Utilisateur userConnecte = getCurrentUser();
            if (!userConnecte.getRole().name().equals("ADMIN") && !interaction.getUtilisateur().getId().equals(userConnecte.getId())) {
                model.addAttribute("error", "Vous n'avez pas la permission d'accéder à cette interaction");
                return "access-denied";
            }

            model.addAttribute("interaction", interaction);
            return "interactions/details";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "access-denied";
        }
    }

    /**
     * Affiche le formulaire de modification d'une interaction
     */
    @GetMapping("/modifier/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        try {
            Interaction interaction = interactionService.getInteractionById(id);
            
            // Vérifier les permissions
            Utilisateur userConnecte = getCurrentUser();
            if (!userConnecte.getRole().name().equals("ADMIN") && !interaction.getUtilisateur().getId().equals(userConnecte.getId())) {
                model.addAttribute("error", "Vous n'avez pas la permission de modifier cette interaction");
                return "access-denied";
            }

            CreerInteractionRequest request = new CreerInteractionRequest();
            request.setType(interaction.getType());
            request.setDateHeure(interaction.getDateHeure());
            request.setResume(interaction.getResume());
            request.setClientId(interaction.getClient().getId());

            model.addAttribute("interaction", request);
            model.addAttribute("client", interaction.getClient());
            model.addAttribute("id", id);
            model.addAttribute("typesInteraction", TypeInteraction.values());
            return "interactions/form";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "access-denied";
        }
    }

    /**
     * Met à jour une interaction existante
     */
    @PostMapping("/modifier/{id}")
    public String updateInteraction(@PathVariable Long id,
                                    @Valid @ModelAttribute("interaction") CreerInteractionRequest request,
                                    BindingResult bindingResult,
                                    Model model,
                                    RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            try {
                Interaction interaction = interactionService.getInteractionById(id);
                model.addAttribute("client", interaction.getClient());
                model.addAttribute("interaction", request);
                model.addAttribute("id", id);
                model.addAttribute("typesInteraction", TypeInteraction.values());
            } catch (Exception e) {
                model.addAttribute("error", e.getMessage());
            }
            return "interactions/form";
        }

        try {
            Utilisateur userConnecte = getCurrentUser();
            interactionService.updateInteraction(id, request, userConnecte);
            redirectAttributes.addFlashAttribute("success", "Interaction modifiée avec succès");
            return "redirect:/interactions/list";
        } catch (Exception e) {
            try {
                Interaction interaction = interactionService.getInteractionById(id);
                model.addAttribute("client", interaction.getClient());
                model.addAttribute("interaction", request);
                model.addAttribute("id", id);
                model.addAttribute("typesInteraction", TypeInteraction.values());
            } catch (Exception ex) {
                model.addAttribute("error", ex.getMessage());
            }
            model.addAttribute("error", "Erreur lors de la modification: " + e.getMessage());
            return "interactions/form";
        }
    }

    /**
     * Supprime une interaction
     */
    @PostMapping("/supprimer/{id}")
    public String deleteInteraction(@PathVariable Long id, 
                                    RedirectAttributes redirectAttributes) {
        try {
            Interaction interaction = interactionService.getInteractionById(id);
            
            Utilisateur userConnecte = getCurrentUser();
            interactionService.deleteInteraction(id, userConnecte);
            
            redirectAttributes.addFlashAttribute("success", "Interaction supprimée avec succès");
            return "redirect:/interactions/list";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la suppression: " + e.getMessage());
            return "redirect:/interactions/list";
        }
    }

    /**
     * Récupère l'utilisateur actuellement connecté
     */
    private Utilisateur getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        return utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
    }
}
