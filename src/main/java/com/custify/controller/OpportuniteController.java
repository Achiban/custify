package com.custify.controller;

import com.custify.dto.CreerOpportuniteRequest;
import com.custify.dto.NextBestActionDTO;
import com.custify.exception.AccesNonAutoriseException;
import com.custify.exception.ClientNonTrouveException;
import com.custify.exception.OpportuniteNonTrouveException;
import com.custify.model.Client;
import com.custify.model.Opportunite;
import com.custify.model.Utilisateur;
import com.custify.model.enums.StatutOpportunite;
import com.custify.repository.ClientRepository;
import com.custify.repository.UtilisateurRepository;
import com.custify.service.ClientService;
import com.custify.service.NextBestActionRecommendationService;
import com.custify.service.OpportuniteService;
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

import java.util.List;

@Controller
@RequestMapping("/opportunites")
public class OpportuniteController {

    @Autowired
    private OpportuniteService opportuniteService;

    @Autowired
    private ClientService clientService;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private NextBestActionRecommendationService nextBestActionService;

    @GetMapping("")
    public String indexOpportunites() {
        return "redirect:/opportunites/list";
    }

    // GET FORM FOR CREATING OPPORTUNITY
    @GetMapping("/new")
    public String showForm(Model model, Long clientId) {
        CreerOpportuniteRequest request = new CreerOpportuniteRequest();
        if (clientId != null) {
            request.setClientId(clientId);
        }
        model.addAttribute("opportuniteRequest", request);
        model.addAttribute("statuts", StatutOpportunite.values());

        // Load clients for dropdown
        Utilisateur user = getLoggedUser();
        List<Client> clients = clientService.getClientsByUser(user);
        model.addAttribute("clients", clients);

        return "opportunites/form";
    }

    // SAVE OPPORTUNITY
    @PostMapping("/save")
    public String saveOpportunite(@ModelAttribute CreerOpportuniteRequest request, Model model) {
        try {
            Utilisateur user = getLoggedUser();
            opportuniteService.saveOpportunite(
                    request.getTitre(),
                    request.getMontant(),
                    request.getStatut(),
                    request.getClientId(),
                    user
            );
            return "redirect:/opportunites/list";
        } catch (ClientNonTrouveException | AccesNonAutoriseException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("opportuniteRequest", request);
            model.addAttribute("statuts", StatutOpportunite.values());
            List<Client> clients = clientService.getClientsByUser(getLoggedUser());
            model.addAttribute("clients", clients);
            return "opportunites/form";
        }
    }

    // LIST OPPORTUNITIES
    @GetMapping("/list")
    public String listOpportunites(Model model, String filtreStatut) {
        Utilisateur user = getLoggedUser();
        List<Opportunite> opportunites;

        if (filtreStatut != null && !filtreStatut.trim().isEmpty()) {
            try {
                StatutOpportunite statut = StatutOpportunite.valueOf(filtreStatut.toUpperCase());
                opportunites = opportuniteService.getOpportunitesByStatut(user, statut);
                model.addAttribute("filtreStatut", filtreStatut);
            } catch (IllegalArgumentException e) {
                opportunites = opportuniteService.getOpportunitesByUser(user);
            }
        } else {
            opportunites = opportuniteService.getOpportunitesByUser(user);
        }

        model.addAttribute("opportunites", opportunites);
        model.addAttribute("statuts", StatutOpportunite.values());
        return "opportunites/list";
    }

    // DETAILS
    @GetMapping("/details/{id}")
    public String details(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Opportunite opportunite = opportuniteService.getOpportuniteForUser(id, getLoggedUser());
            model.addAttribute("opportunite", opportunite);

            // Ajouter la prochaine action idéale (Next Best Action)
            NextBestActionDTO nextBestAction = nextBestActionService.getNextBestActionForOpportunite(opportunite);
            model.addAttribute("nextBestAction", nextBestAction);

            return "opportunites/details";
        } catch (OpportuniteNonTrouveException | AccesNonAutoriseException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/opportunites/list";
        }
    }

    // EDIT FORM
    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Opportunite opportunite = opportuniteService.getOpportuniteForUser(id, getLoggedUser());
            model.addAttribute("opportunite", opportunite);
            model.addAttribute("statuts", StatutOpportunite.values());

            // Load clients for dropdown
            Utilisateur user = getLoggedUser();
            List<Client> clients = clientService.getClientsByUser(user);
            model.addAttribute("clients", clients);

            return "opportunites/edit";
        } catch (OpportuniteNonTrouveException | AccesNonAutoriseException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/opportunites/list";
        }
    }

    // UPDATE OPPORTUNITY
    @PostMapping("/update/{id}")
    public String updateOpportunite(
            @PathVariable Long id,
            @ModelAttribute CreerOpportuniteRequest request,
            Model model,
            RedirectAttributes redirectAttributes) {

        try {
            Utilisateur user = getLoggedUser();
            opportuniteService.updateOpportunite(
                    id,
                    request.getTitre(),
                    request.getMontant(),
                    request.getStatut(),
                    user
            );
            redirectAttributes.addFlashAttribute("success", "L'opportunite a ete modifiee avec succes.");
            return "redirect:/opportunites/details/" + id;
        } catch (OpportuniteNonTrouveException | AccesNonAutoriseException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/opportunites/list";
        }
    }

    // DELETE OPPORTUNITY
    @PostMapping("/delete/{id}")
    public String deleteOpportunite(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            opportuniteService.deleteOpportunite(id, getLoggedUser());
            redirectAttributes.addFlashAttribute("success", "L'opportunite a ete supprimee avec succes.");
        } catch (OpportuniteNonTrouveException | AccesNonAutoriseException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/opportunites/list";
    }

    private Utilisateur getLoggedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        return utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}