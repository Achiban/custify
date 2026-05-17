package com.custify.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Redirects legacy /users/** routes to the new /admin/users/** routes.
 * Kept for backward compatibility.
 */
@Controller
@RequestMapping("/users")
public class UtilisateurController {

    @GetMapping
    public String redirectToAdmin() {
        return "redirect:/admin/users";
    }
}

