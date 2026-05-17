package com.custify.controller;

import com.custify.dto.CreerAffectationRequest;
import com.custify.model.Affectation;
import com.custify.model.Reunion;
import com.custify.model.Utilisateur;
import com.custify.model.enums.Role;
import com.custify.model.enums.StatutAffectation;
import com.custify.repository.UtilisateurRepository;
import com.custify.service.AffectationService;
import com.custify.service.DemandeService;
import com.custify.service.OpportuniteMarketplaceService;
import com.custify.service.ReunionService;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/commercial")
public class CommercialController {

    private final OpportuniteMarketplaceService opportuniteService;
    private final DemandeService demandeService;
    private final AffectationService affectationService;
    private final UtilisateurRepository utilisateurRepository;
    private final ReunionService reunionService;

    public CommercialController(OpportuniteMarketplaceService opportuniteService,
                                DemandeService demandeService,
                                AffectationService affectationService,
                                UtilisateurRepository utilisateurRepository,
                                ReunionService reunionService) {
        this.opportuniteService = opportuniteService;
        this.demandeService = demandeService;
        this.affectationService = affectationService;
        this.utilisateurRepository = utilisateurRepository;
        this.reunionService = reunionService;
    }

    private Utilisateur getCommercial(UserDetails userDetails) {
        return utilisateurRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
    }

    // ── Dashboard (Vue d'ensemble) ─────────────────────────────────────────────
    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        Utilisateur commercial = getCommercial(userDetails);
        model.addAttribute("commercial", commercial);

        // ── Demandes à traiter ────────────────────────────────────────────────
        model.addAttribute("totalDemandes", demandeService.listerEnAttente().size());

        // ── Mes affectations (breakdown par statut) ───────────────────────────
        List<Affectation> mesAffectations = affectationService.listerParCommercial(commercial);
        long affEnAttente = mesAffectations.stream()
                .filter(a -> a.getStatutClient() == StatutAffectation.EN_ATTENTE).count();
        long affAcceptees = mesAffectations.stream()
                .filter(a -> a.getStatutClient() == StatutAffectation.ACCEPTEE).count();
        long affRefusees  = mesAffectations.stream()
                .filter(a -> a.getStatutClient() == StatutAffectation.REFUSEE).count();
        long clientsGeres = mesAffectations.stream()
                .filter(a -> a.getStatutClient() == StatutAffectation.ACCEPTEE)
                .map(a -> a.getClientBeneficiaire().getId())
                .distinct().count();
        int tauxSucces = (affAcceptees + affRefusees) > 0
                ? (int) (affAcceptees * 100L / (affAcceptees + affRefusees)) : 0;

        model.addAttribute("totalAffectations", mesAffectations.size());
        model.addAttribute("affEnAttente",  affEnAttente);
        model.addAttribute("affAcceptees",  affAcceptees);
        model.addAttribute("affRefusees",   affRefusees);
        model.addAttribute("clientsGeres",  clientsGeres);
        model.addAttribute("tauxSuccesAffectations", tauxSucces);

        // ── Réunions (total + 3 prochaines) ──────────────────────────────────
        List<Reunion> mesReunions = reunionService.listerParCommercial(commercial);
        List<Reunion> prochainesReunions = mesReunions.stream()
                .filter(r -> r.getDateReunion().isAfter(LocalDateTime.now()))
                .sorted(Comparator.comparing(Reunion::getDateReunion))
                .limit(3)
                .toList();
        model.addAttribute("totalReunions", mesReunions.size());
        model.addAttribute("prochainesReunions", prochainesReunions);

        // ── Opportunités disponibles sur la plateforme ────────────────────────
        model.addAttribute("totalOppDisponibles", opportuniteService.listerDisponibles().size());

        return "commercial/dashboard";
    }

    // ── Page Demandes ──────────────────────────────────────────────────────────
    @GetMapping("/demandes")
    public String demandes(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        Utilisateur commercial = getCommercial(userDetails);
        model.addAttribute("commercial", commercial);
        model.addAttribute("demandesEnAttente", demandeService.listerEnAttente());
        model.addAttribute("opportunitesDisponibles", opportuniteService.listerDisponibles());
        model.addAttribute("clients", utilisateurRepository.findByRole(Role.CLIENT));
        model.addAttribute("affectationRequest", new CreerAffectationRequest());
        return "commercial/demandes";
    }

    @PostMapping("/demandes/{id}/accepter")
    public String accepterDemande(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails, RedirectAttributes redirectAttributes) {
        try {
            Utilisateur commercial = getCommercial(userDetails);
            demandeService.accepterDemande(id, commercial);
            redirectAttributes.addFlashAttribute("message", "Demande acceptée ! Une affectation a été créée pour discuter avec le client.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/commercial/demandes";
    }

    @PostMapping("/demandes/{id}/refuser")
    public String refuserDemande(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            demandeService.refuserDemande(id);
            redirectAttributes.addFlashAttribute("message", "Demande refusée.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/commercial/demandes";
    }

    // ── Page Opportunités ──────────────────────────────────────────────────────
    @GetMapping("/opportunites")
    public String opportunites(Model model) {
        model.addAttribute("opportunitesDisponibles", opportuniteService.listerDisponibles());
        return "commercial/opportunites";
    }

    // ── Page Clients ───────────────────────────────────────────────────────────
    @GetMapping("/clients")
    public String clients(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        model.addAttribute("commercial", getCommercial(userDetails));
        model.addAttribute("clients", utilisateurRepository.findByRole(Role.CLIENT));
        return "commercial/clients";
    }

    // ── Page Affectations ──────────────────────────────────────────────────────
    @GetMapping("/affectations")
    public String affectations(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        Utilisateur commercial = getCommercial(userDetails);
        model.addAttribute("commercial", commercial);
        model.addAttribute("mesAffectations", affectationService.listerParCommercial(commercial));
        model.addAttribute("opportunitesDisponibles", opportuniteService.listerDisponibles());
        model.addAttribute("clients", utilisateurRepository.findByRole(Role.CLIENT));
        model.addAttribute("affectationRequest", new CreerAffectationRequest());
        return "commercial/affectations";
    }

    // ── Page Réunions ──────────────────────────────────────────────────────────
    @GetMapping("/reunions")
    public String reunions(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        Utilisateur commercial = getCommercial(userDetails);
        model.addAttribute("commercial", commercial);
        model.addAttribute("mesReunions", reunionService.listerParCommercial(commercial));
        return "commercial/reunions";
    }


    // ── Affectations ──────────────────────────────────────────────────────────
    @PostMapping("/affectations")
    public String creerAffectation(@Valid @ModelAttribute("affectationRequest") CreerAffectationRequest request,
                                   BindingResult result,
                                   @AuthenticationPrincipal UserDetails userDetails,
                                   RedirectAttributes redirectAttributes,
                                   Model model) {
        if (result.hasErrors()) {
            Utilisateur commercial = getCommercial(userDetails);
            model.addAttribute("commercial", commercial);
            model.addAttribute("opportunitesDisponibles", opportuniteService.listerDisponibles());
            model.addAttribute("clients", utilisateurRepository.findByRole(Role.CLIENT));
            model.addAttribute("mesAffectations", affectationService.listerParCommercial(commercial));
            return "commercial/affectations";
        }
        try {
            Utilisateur commercial = getCommercial(userDetails);
            affectationService.creerAffectation(request, commercial);
            redirectAttributes.addFlashAttribute("message", "Affectation créée avec succès !");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/commercial/affectations";
    }

    // ── Réunions ──────────────────────────────────────────────────────────────
    @GetMapping("/affectations/{id}/reunion")
    public String planifierReunionForm(@PathVariable Long id,
                                       @AuthenticationPrincipal UserDetails userDetails,
                                       Model model) {
        Utilisateur commercial = getCommercial(userDetails);
        var affectation = affectationService.listerParCommercial(commercial).stream()
                .filter(a -> a.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Affectation non trouvée ou non autorisée"));

        model.addAttribute("affectation", affectation);
        return "commercial/reunion-form";
    }

    @PostMapping("/affectations/{id}/reunion")
    public String planifierReunion(@PathVariable Long id,
                                   @RequestParam String dateReunion,
                                   @RequestParam String sujet,
                                   @RequestParam String lieu,
                                   @RequestParam String description,
                                   @AuthenticationPrincipal UserDetails userDetails,
                                   RedirectAttributes redirectAttributes) {
        try {
            Utilisateur commercial = getCommercial(userDetails);
            java.time.LocalDateTime dt = java.time.LocalDateTime.parse(dateReunion);
            reunionService.organiserReunion(id, dt, sujet, lieu, description, commercial);
            redirectAttributes.addFlashAttribute("message", "Réunion planifiée avec succès !");
        } catch (java.time.format.DateTimeParseException e) {
            redirectAttributes.addFlashAttribute("error", "Format de date invalide. Veuillez vérifier la date saisie.");
            return "redirect:/commercial/affectations/" + id + "/reunion";
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Une erreur est survenue lors de la planification de la réunion : " + e.getMessage());
        }
        return "redirect:/commercial/reunions";
    }

    // ── Gestion des Clients (Consultation / Modification / Suppression) ────────
    @GetMapping("/clients/{id}/modifier")
    public String modifierClientForm(@PathVariable Long id, Model model) {
        Utilisateur client = utilisateurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Client non trouvé: " + id));
        model.addAttribute("client", client);
        return "commercial/modifier-client";
    }

    @PostMapping("/clients/{id}/modifier")
    public String modifierClient(@PathVariable Long id,
                                 @RequestParam String nom,
                                 @RequestParam String prenom,
                                 @RequestParam String email,
                                 @RequestParam String telephone,
                                 @RequestParam String entreprise,
                                 @RequestParam String adresse,
                                 RedirectAttributes redirectAttributes) {
        Utilisateur client = utilisateurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Client non trouvé: " + id));

        // Vérifier l'unicité de l'email si modifié
        if (!client.getEmail().equalsIgnoreCase(email) && utilisateurRepository.findByEmail(email).isPresent()) {
            redirectAttributes.addFlashAttribute("error", "L'adresse email est déjà utilisée.");
            return "redirect:/commercial/clients/" + id + "/modifier";
        }

        client.setNom(nom);
        client.setPrenom(prenom);
        client.setEmail(email);
        client.setTelephone(telephone);
        client.setEntreprise(entreprise);
        client.setAdresse(adresse);
        utilisateurRepository.save(client);

        redirectAttributes.addFlashAttribute("message", "Le client a été modifié avec succès !");
        return "redirect:/commercial/clients";
    }

    @PostMapping("/clients/{id}/supprimer")
    public String supprimerClient(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Utilisateur client = utilisateurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Client non trouvé: " + id));
        utilisateurRepository.delete(client);
        redirectAttributes.addFlashAttribute("message", "Le client a été supprimé avec succès !");
        return "redirect:/commercial/clients";
    }
}
