package com.custify.controller;

import com.custify.service.DashboardService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class AuthController {

    private final DashboardService dashboardService;

    public AuthController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/login")
    public String showLoginPage(@RequestParam(value = "error", required = false) String error,
                                 @RequestParam(value = "logout", required = false) String logout,
                                 Model model) {
        if (error != null) {
            model.addAttribute("error", "Invalid email or password");
        }
        if (logout != null) {
            model.addAttribute("message", "You have been logged out successfully");
        }
        return "login";
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        DashboardService.DashboardStats stats = dashboardService.getStats();
        model.addAttribute("stats", stats);
        return "dashboard";
    }

    @GetMapping("/access-denied")
    public String accessDenied() {
        return "access-denied";
    }
}
