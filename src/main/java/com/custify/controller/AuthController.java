package com.custify.controller;

import com.custify.dto.InscriptionRequest;
import com.custify.service.InscriptionService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    private final InscriptionService inscriptionService;

    public AuthController(InscriptionService inscriptionService) {
        this.inscriptionService = inscriptionService;
    }

    @GetMapping("/login")
    public String showLoginPage(@RequestParam(value = "error", required = false) String error,
                                @RequestParam(value = "logout", required = false) String logout,
                                Model model) {
        if (error != null) {
            model.addAttribute("error", "Email ou mot de passe invalide");
        }
        if (logout != null) {
            model.addAttribute("message", "Vous avez été déconnecté avec succès");
        }
        return "login";
    }

    @GetMapping("/access-denied")
    public String accessDenied() {
        return "access-denied";
    }

    @GetMapping("/inscription")
    public String showInscriptionPage(Model model) {
        model.addAttribute("inscriptionRequest", new InscriptionRequest());
        model.addAttribute("secteurs", inscriptionService.listerSecteurs());
        return "inscription";
    }

    @PostMapping("/inscription")
    public String inscrire(@Valid @ModelAttribute("inscriptionRequest") InscriptionRequest request,
                           BindingResult result,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("secteurs", inscriptionService.listerSecteurs());
            return "inscription";
        }
        try {
            inscriptionService.inscrireClient(request);
            redirectAttributes.addFlashAttribute("message",
                    "Compte créé avec succès ! Vous pouvez maintenant vous connecter.");
            return "redirect:/login";
        } catch (com.custify.exception.EmailDejaUtiliseException e) {
            model.addAttribute("emailError", "Cet email est déjà utilisé");
            model.addAttribute("secteurs", inscriptionService.listerSecteurs());
            return "inscription";
        }
    }
}

