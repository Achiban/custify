package com.custify.controller;

import com.custify.dto.CreerUtilisateurRequest;
import com.custify.dto.ModifierRoleRequest;
import com.custify.dto.UtilisateurResponse;
import com.custify.model.enums.Role;
import com.custify.model.enums.StatutAffectation;
import com.custify.model.enums.StatutDemande;
import com.custify.model.enums.StatutOpportunite;
import com.custify.repository.AffectationRepository;
import com.custify.repository.DemandeOpportuniteRepository;
import com.custify.repository.OpportuniteRepository;
import com.custify.repository.ReunionRepository;
import com.custify.repository.UtilisateurRepository;
import com.custify.service.UtilisateurService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UtilisateurService utilisateurService;
    private final UtilisateurRepository utilisateurRepository;
    private final OpportuniteRepository opportuniteRepository;
    private final DemandeOpportuniteRepository demandeRepository;
    private final AffectationRepository affectationRepository;
    private final ReunionRepository reunionRepository;

    public AdminController(UtilisateurService utilisateurService,
                           UtilisateurRepository utilisateurRepository,
                           OpportuniteRepository opportuniteRepository,
                           DemandeOpportuniteRepository demandeRepository,
                           AffectationRepository affectationRepository,
                           ReunionRepository reunionRepository) {
        this.utilisateurService = utilisateurService;
        this.utilisateurRepository = utilisateurRepository;
        this.opportuniteRepository = opportuniteRepository;
        this.demandeRepository = demandeRepository;
        this.affectationRepository = affectationRepository;
        this.reunionRepository = reunionRepository;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        // ── Utilisateurs ──────────────────────────────────────────────────────
        long totalClients     = utilisateurRepository.findByRole(Role.CLIENT).size();
        long totalCommerciaux = utilisateurRepository.findByRole(Role.COMMERCIAL).size();
        long totalAdmins      = utilisateurRepository.findByRole(Role.ADMIN).size();
        model.addAttribute("totalUtilisateurs", utilisateurRepository.count());
        model.addAttribute("totalClients",      totalClients);
        model.addAttribute("totalCommerciaux",  totalCommerciaux);
        model.addAttribute("totalAdmins",       totalAdmins);

        // ── Opportunités ──────────────────────────────────────────────────────
        long oppDisponibles = opportuniteRepository.findByStatut(StatutOpportunite.DISPONIBLE).size();
        long oppAttribuees  = opportuniteRepository.findByStatut(StatutOpportunite.ATTRIBUEE).size();
        long oppConclues    = opportuniteRepository.findByStatut(StatutOpportunite.CONCLUE).size();
        model.addAttribute("totalOpportunites",   opportuniteRepository.count());
        model.addAttribute("totalOppDisponibles", oppDisponibles);
        model.addAttribute("totalOppAttribuees",  oppAttribuees);
        model.addAttribute("totalOppConclues",    oppConclues);

        // ── Demandes ──────────────────────────────────────────────────────────
        long totalDemandes       = demandeRepository.count();
        long demandesEnAttente   = demandeRepository.findByStatut(StatutDemande.EN_ATTENTE).size();
        long demandesAcceptees   = demandeRepository.findByStatut(StatutDemande.ACCEPTEE).size();
        long demandesRefusees    = demandeRepository.findByStatut(StatutDemande.REFUSEE).size();
        int  tauxConversion      = totalDemandes > 0 ? (int) (demandesAcceptees * 100L / totalDemandes) : 0;
        model.addAttribute("totalDemandes",          totalDemandes);
        model.addAttribute("totalDemandesEnAttente", demandesEnAttente);
        model.addAttribute("totalDemandesAcceptees", demandesAcceptees);
        model.addAttribute("totalDemandesRefusees",  demandesRefusees);
        model.addAttribute("tauxConversionDemandes", tauxConversion);

        // ── Affectations ──────────────────────────────────────────────────────
        long affEnAttente = affectationRepository.findByStatutClient(StatutAffectation.EN_ATTENTE).size();
        long affAcceptees = affectationRepository.findByStatutClient(StatutAffectation.ACCEPTEE).size();
        long affRefusees  = affectationRepository.findByStatutClient(StatutAffectation.REFUSEE).size();
        model.addAttribute("totalAffectations",  affectationRepository.count());
        model.addAttribute("totalAffEnAttente",  affEnAttente);
        model.addAttribute("totalAffAcceptees",  affAcceptees);
        model.addAttribute("totalAffRefusees",   affRefusees);

        // ── Réunions ──────────────────────────────────────────────────────────
        model.addAttribute("totalReunions", reunionRepository.count());

        return "admin/dashboard";
    }

    @GetMapping("/users")
    public String listerInternes(Model model) {
        java.util.List<UtilisateurResponse> tous = utilisateurService.listerTous();
        java.util.List<UtilisateurResponse> internes = tous.stream().filter(u -> u.getRole() == Role.COMMERCIAL || u.getRole() == Role.ADMIN).toList();
        model.addAttribute("utilisateurs", internes);
        return "users/list";
    }

    @GetMapping("/clients")
    public String listerClients(Model model) {
        java.util.List<UtilisateurResponse> tous = utilisateurService.listerTous();
        java.util.List<UtilisateurResponse> clients = tous.stream().filter(u -> u.getRole() == Role.CLIENT).toList();
        model.addAttribute("clients", clients);
        return "users/clients";
    }

    @GetMapping("/users/nouveau")
    public String afficherFormulaireCreation(Model model) {
        model.addAttribute("utilisateur", new CreerUtilisateurRequest());
        return "users/form";
    }

    @PostMapping("/users")
    public String creer(@Valid @ModelAttribute("utilisateur") CreerUtilisateurRequest requete,
                        BindingResult result, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) return "users/form";
        UtilisateurResponse cree = utilisateurService.creer(requete);
        redirectAttributes.addFlashAttribute("message", "Utilisateur créé : " + cree.getNom());
        return "redirect:/admin/users";
    }

    @GetMapping("/users/{id}/modifier-role")
    public String afficherFormulaireRole(@PathVariable Long id, Model model) {
        UtilisateurResponse utilisateur = utilisateurService.trouverParId(id);
        ModifierRoleRequest request = new ModifierRoleRequest();
        request.setRole(utilisateur.getRole());
        model.addAttribute("utilisateur", utilisateur);
        model.addAttribute("modifierRoleRequest", request);
        return "users/edit-role";
    }

    @PostMapping("/users/{id}/modifier-role")
    public String modifierRole(@PathVariable Long id,
                               @Valid @ModelAttribute("modifierRoleRequest") ModifierRoleRequest request,
                               BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("utilisateur", utilisateurService.trouverParId(id));
            return "users/edit-role";
        }
        utilisateurService.modifierRole(id, request.getRole());
        redirectAttributes.addFlashAttribute("message", "Rôle modifié avec succès");
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/supprimer")
    public String supprimer(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        utilisateurService.supprimer(id);
        redirectAttributes.addFlashAttribute("message", "Utilisateur supprimé avec succès");
        return "redirect:/admin/users";
    }
}
