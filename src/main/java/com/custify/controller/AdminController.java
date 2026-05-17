package com.custify.controller;

import com.custify.dto.CreerUtilisateurRequest;
import com.custify.dto.ModifierRoleRequest;
import com.custify.dto.UtilisateurResponse;
import com.custify.model.enums.Role;
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

    public AdminController(UtilisateurService utilisateurService,
                           UtilisateurRepository utilisateurRepository) {
        this.utilisateurService = utilisateurService;
        this.utilisateurRepository = utilisateurRepository;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("totalUtilisateurs", utilisateurRepository.count());
        model.addAttribute("totalClients", utilisateurRepository.findByRole(Role.CLIENT).size());
        model.addAttribute("totalCommerciaux", utilisateurRepository.findByRole(Role.COMMERCIAL).size());
        return "admin/dashboard";
    }

    @GetMapping("/users")
    public String lister(Model model) {
        model.addAttribute("utilisateurs", utilisateurService.listerTous());
        return "users/list";
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
