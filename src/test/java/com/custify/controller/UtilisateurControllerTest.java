package com.custify.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class UtilisateurControllerTest {

    private final UtilisateurController controller = new UtilisateurController();

    @Test
    void redirectToAdminShouldReturnRedirect() {
        assertEquals("redirect:/admin/users", controller.redirectToAdmin());
    }
}
