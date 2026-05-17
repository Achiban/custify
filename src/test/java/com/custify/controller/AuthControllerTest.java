package com.custify.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.custify.service.InscriptionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ExtendedModelMap;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private InscriptionService inscriptionService;

    private AuthController authController;

    @BeforeEach
    void setUp() {
        authController = new AuthController(inscriptionService);
    }

    @Test
    void showLoginPageShouldExposeErrorAndLogoutMessages() {
        ExtendedModelMap model = new ExtendedModelMap();

        String viewName = authController.showLoginPage("true", "true", model);

        assertEquals("login", viewName);
        assertEquals("Email ou mot de passe invalide", model.getAttribute("error"));
        assertEquals("Vous avez été déconnecté avec succès", model.getAttribute("message"));
    }

    @Test
    void accessDeniedShouldReturnAccessDeniedView() {
        assertEquals("access-denied", authController.accessDenied());
    }
}
