package com.custify.controller;

import com.custify.model.Opportunite;
import com.custify.model.enums.StatutOpportunite;
import com.custify.repository.OpportuniteRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class PublicController {

    private final OpportuniteRepository opportuniteRepository;

    public PublicController(OpportuniteRepository opportuniteRepository) {
        this.opportuniteRepository = opportuniteRepository;
    }

    /** Page d'accueil publique - aperçu des opportunités disponibles */
    @GetMapping("/")
    public String index(Model model) {
        List<Opportunite> disponibles = opportuniteRepository.findByStatut(StatutOpportunite.DISPONIBLE);
        model.addAttribute("opportunites", disponibles);
        return "index";
    }

    /** Alias public pour les opportunités */
    @GetMapping("/opportunites/public")
    public String publicOpportunites(Model model) {
        List<Opportunite> disponibles = opportuniteRepository.findByStatut(StatutOpportunite.DISPONIBLE);
        model.addAttribute("opportunites", disponibles);
        return "index";
    }
}
