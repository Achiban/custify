package com.custify.controller;

import java.util.List;

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

import com.custify.exception.ProspectNonTrouveException;
import com.custify.model.Prospect;
import com.custify.model.Utilisateur;
import com.custify.model.enums.StatutProspect;
import com.custify.repository.UtilisateurRepository;
import com.custify.service.ProspectService;

@Controller
@RequestMapping("/prospects")
public class ProspectController {

    @Autowired
    private ProspectService prospectService;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @GetMapping("")
    public String indexProspects() {
        return "redirect:/prospects/list";
    }

    @GetMapping("/list")
    public String listProspects(Model model) {
        List<Prospect> prospects = prospectService.getAllProspects();
        model.addAttribute("prospects", prospects);
        return "prospects/list";
    }

    @GetMapping("/new")
    public String showNewForm(Model model) {
        model.addAttribute("prospect", new Prospect());
        model.addAttribute("statuts", StatutProspect.values());
        return "prospects/form";
    }

    @PostMapping("/save")
    public String saveProspect(@ModelAttribute Prospect prospect, Model model, RedirectAttributes redirectAttributes) {
        try {
            Utilisateur user = getLoggedUser();
            prospectService.saveProspect(prospect, user);
            redirectAttributes.addFlashAttribute("success", "Prospect créé avec succès.");
            return "redirect:/prospects/list";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("prospect", prospect);
            model.addAttribute("statuts", StatutProspect.values());
            return "prospects/form";
        }
    }

    @GetMapping("/details/{id}")
    public String details(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Prospect prospect = prospectService.getProspectById(id);
            model.addAttribute("prospect", prospect);
            return "prospects/details";
        } catch (ProspectNonTrouveException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/prospects/list";
        }
    }

    private Utilisateur getLoggedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        return utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé."));
    }
}
