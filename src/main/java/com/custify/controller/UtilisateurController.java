package com.custify.controller;

import com.custify.dto.CreerUtilisateurRequest;
import com.custify.dto.UtilisateurResponse;
import com.custify.service.UtilisateurService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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
        redirectAttributes.addFlashAttribute("message", "User created successfully with ID: " + cree.getId());
        return "redirect:/users";
    }
}
