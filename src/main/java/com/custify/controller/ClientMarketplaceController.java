package com.custify.controller;

import com.custify.dto.CreerOpportuniteMarketplaceRequest;
import com.custify.model.Utilisateur;
import com.custify.repository.UtilisateurRepository;
import com.custify.service.AffectationService;
import com.custify.service.DemandeService;
import com.custify.service.OpportuniteMarketplaceService;
import com.custify.service.ReunionService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/client")
public class ClientMarketplaceController {

    private final OpportuniteMarketplaceService opportuniteService;
    private final DemandeService demandeService;
    private final AffectationService affectationService;
    private final UtilisateurRepository utilisateurRepository;
    private final ReunionService reunionService;

    public ClientMarketplaceController(OpportuniteMarketplaceService opportuniteService,
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

    private Utilisateur getClient(UserDetails userDetails) {
        return utilisateurRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
    }

    // ── Dashboard ─────────────────────────────────────────────────────────────
    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        Utilisateur client = getClient(userDetails);
        model.addAttribute("client", client);
        model.addAttribute("mesOpportunites", opportuniteService.listerParVendeur(client));
        model.addAttribute("opportunitesDisponibles",
                opportuniteService.listerDisponibles().stream()
                        .filter(o -> !o.getClientVendeur().getId().equals(client.getId()))
                        .toList());
        model.addAttribute("mesDemandes", demandeService.listerParClient(client));
        model.addAttribute("mesAffectations", affectationService.listerParClient(client));
        model.addAttribute("mesReunions", reunionService.listerParClient(client));
        return "client/dashboard";
    }

    // ── Opportunités ──────────────────────────────────────────────────────────
    @GetMapping("/opportunites/nouvelle")
    public String formulaireNouvelleOpportunite(Model model) {
        model.addAttribute("opportuniteRequest", new CreerOpportuniteMarketplaceRequest());
        return "client/opportunite-form";
    }

    @PostMapping("/opportunites")
    public String publierOpportunite(@Valid @ModelAttribute("opportuniteRequest") CreerOpportuniteMarketplaceRequest request,
                                     BindingResult result,
                                     @AuthenticationPrincipal UserDetails userDetails,
                                     RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) return "client/opportunite-form";
        Utilisateur client = getClient(userDetails);
        opportuniteService.publier(request, client);
        redirectAttributes.addFlashAttribute("message", "Opportunité publiée avec succès !");
        return "redirect:/client/dashboard";
    }

    @GetMapping("/opportunites/{id}/modifier")
    public String formulaireModification(@PathVariable Long id,
                                         @AuthenticationPrincipal UserDetails userDetails,
                                         Model model) {
        Utilisateur client = getClient(userDetails);
        var opp = opportuniteService.trouverParId(id);
        if (!opp.getClientVendeur().getId().equals(client.getId())) {
            return "redirect:/client/dashboard";
        }
        var req = new CreerOpportuniteMarketplaceRequest();
        req.setTitre(opp.getTitre());
        req.setDescriptionComplete(opp.getDescriptionComplete());
        req.setMontant(opp.getMontant());
        req.setCategorie(opp.getCategorie());
        model.addAttribute("opportuniteRequest", req);
        model.addAttribute("opportuniteId", id);
        return "client/opportunite-form";
    }

    @PostMapping("/opportunites/{id}/modifier")
    public String modifierOpportunite(@PathVariable Long id,
                                      @Valid @ModelAttribute("opportuniteRequest") CreerOpportuniteMarketplaceRequest request,
                                      BindingResult result,
                                      @AuthenticationPrincipal UserDetails userDetails,
                                      RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) return "client/opportunite-form";
        Utilisateur client = getClient(userDetails);
        opportuniteService.modifier(id, request, client);
        redirectAttributes.addFlashAttribute("message", "Opportunité modifiée avec succès !");
        return "redirect:/client/dashboard";
    }

    @PostMapping("/opportunites/{id}/supprimer")
    public String supprimerOpportunite(@PathVariable Long id,
                                       @AuthenticationPrincipal UserDetails userDetails,
                                       RedirectAttributes redirectAttributes) {
        Utilisateur client = getClient(userDetails);
        opportuniteService.supprimer(id, client);
        redirectAttributes.addFlashAttribute("message", "Opportunité supprimée.");
        return "redirect:/client/dashboard";
    }

    @GetMapping("/opportunites/{id}")
    public String voirDetailOpportunite(@PathVariable Long id,
                                         @AuthenticationPrincipal UserDetails userDetails,
                                         Model model) {
        Utilisateur client = getClient(userDetails);
        var opp = opportuniteService.trouverParId(id);
        boolean estLeVendeur = opp.getClientVendeur().getId().equals(client.getId());
        boolean dejaDemande = demandeService.listerParClient(client).stream()
                .anyMatch(d -> d.getOpportunite().getId().equals(id));

        model.addAttribute("opportunite", opp);
        model.addAttribute("estLeVendeur", estLeVendeur);
        model.addAttribute("dejaDemande", dejaDemande);
        return "client/opportunite-detail";
    }

    // ── Demandes ──────────────────────────────────────────────────────────────
    @PostMapping("/demandes")
    public String creerDemande(@RequestParam Long opportuniteId,
                               @AuthenticationPrincipal UserDetails userDetails,
                               RedirectAttributes redirectAttributes) {
        Utilisateur client = getClient(userDetails);
        try {
            demandeService.creerDemande(opportuniteId, client);
            redirectAttributes.addFlashAttribute("message", "Votre demande a été envoyée !");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/client/dashboard";
    }

    // ── Affectations ──────────────────────────────────────────────────────────
    @PostMapping("/affectations/{id}/accepter")
    public String accepterAffectation(@PathVariable Long id,
                                      @AuthenticationPrincipal UserDetails userDetails,
                                      RedirectAttributes redirectAttributes) {
        Utilisateur client = getClient(userDetails);
        affectationService.accepterAffectation(id, client);
        redirectAttributes.addFlashAttribute("message", "Affectation acceptée ! L'opportunité est conclue.");
        return "redirect:/client/dashboard";
    }

    @PostMapping("/affectations/{id}/refuser")
    public String refuserAffectation(@PathVariable Long id,
                                     @AuthenticationPrincipal UserDetails userDetails,
                                     RedirectAttributes redirectAttributes) {
        Utilisateur client = getClient(userDetails);
        affectationService.refuserAffectation(id, client);
        redirectAttributes.addFlashAttribute("message", "Affectation refusée.");
        return "redirect:/client/dashboard";
    }
}
