package com.custify.controller;

import com.custify.dto.CreerUtilisateurRequest;
import com.custify.dto.ModifierRoleRequest;
import com.custify.dto.UtilisateurResponse;
import com.custify.service.UtilisateurService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/users")
@PreAuthorize("hasRole('ADMIN')")
public class UtilisateurController {

    private final UtilisateurService utilisateurService;

    public UtilisateurController(UtilisateurService utilisateurService) {
        this.utilisateurService = utilisateurService;
    }

    @GetMapping
    public String lister(Model model) {
        model.addAttribute("utilisateurs", utilisateurService.listerTous());
        return "users/list";
    }

    @GetMapping("/nouveau")
    public String afficherFormulaireCreation(Model model) {
        model.addAttribute("utilisateur", new CreerUtilisateurRequest());
        return "users/form";
    }

    @PostMapping
    public String creer(@Valid @ModelAttribute("utilisateur") CreerUtilisateurRequest requete,
                        BindingResult result,
                        RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "users/form";
        }

        UtilisateurResponse cree = utilisateurService.creer(requete);
        redirectAttributes.addFlashAttribute("message", "Utilisateur créé avec succès avec l'ID: " + cree.getId());
        return "redirect:/users";
    }

    @GetMapping("/{id}/modifier-role")
    public String afficherFormulaireModificationRole(@PathVariable Long id, Model model) {
        UtilisateurResponse utilisateur = utilisateurService.trouverParId(id);
        ModifierRoleRequest request = new ModifierRoleRequest();
        request.setRole(utilisateur.getRole());

        model.addAttribute("utilisateur", utilisateur);
        model.addAttribute("modifierRoleRequest", request);
        return "users/edit-role";
    }

    @PostMapping("/{id}/modifier-role")
    public String modifierRole(@PathVariable Long id,
                               @Valid @ModelAttribute("modifierRoleRequest") ModifierRoleRequest request,
                               BindingResult result,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            UtilisateurResponse utilisateur = utilisateurService.trouverParId(id);
            model.addAttribute("utilisateur", utilisateur);
            return "users/edit-role";
        }

        utilisateurService.modifierRole(id, request.getRole());
        redirectAttributes.addFlashAttribute("message", "Rôle modifié avec succès");
        return "redirect:/users";
    }
}
