package com.custify.controller;

import com.custify.model.Utilisateur;
import com.custify.repository.UtilisateurRepository;
import java.security.Principal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthController {

    private final UtilisateurRepository utilisateurRepository;

    public AuthController(UtilisateurRepository utilisateurRepository) {
        this.utilisateurRepository = utilisateurRepository;
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/dashboard";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, Principal principal) {
        Utilisateur utilisateur = utilisateurRepository.findByEmail(principal.getName()).orElseThrow();
        model.addAttribute("utilisateur", utilisateur);
        return "dashboard";
    }

    @GetMapping("/admin")
    public String adminSpace(Model model, Principal principal) {
        Utilisateur utilisateur = utilisateurRepository.findByEmail(principal.getName()).orElseThrow();
        model.addAttribute("utilisateur", utilisateur);
        return "admin-space";
    }

    @GetMapping("/commercial")
    public String commercialSpace(Model model, Principal principal) {
        Utilisateur utilisateur = utilisateurRepository.findByEmail(principal.getName()).orElseThrow();
        model.addAttribute("utilisateur", utilisateur);
        return "commercial-space";
    }
}
