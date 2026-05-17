package com.custify.system;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/**
 * TS-S4 — Vérifications de performance de base.
 *
 * Objectif : détecter du code manifestement lent (N+1 queries, boucles coûteuses).
 * Seuil : 500 ms par requête (MockMvc in-process, H2 in-memory — pas un benchmark prod).
 *
 * Les tests sont ordonnés pour que le warm-up du contexte Spring
 * (déjà partagé avec les autres classes @ActiveProfiles("test")) ne
 * fausse pas les mesures des tests suivants.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Performance — Temps de réponse de base (seuil 500 ms)")
class PerformanceBaselineTest {

    private static final long SEUIL_MS = 500;

    @Autowired
    private MockMvc mockMvc;

    // ── Endpoints publics ───────────────────────────────────────────────────

    @Test
    @Order(1)
    @DisplayName("GET / (catalogue public) < 500 ms")
    void indexEstRapide() throws Exception {
        long debut = System.currentTimeMillis();
        mockMvc.perform(get("/")).andExpect(status().isOk());
        assertTempsOk("GET /", debut);
    }

    @Test
    @Order(2)
    @DisplayName("GET /login < 500 ms")
    void loginEstRapide() throws Exception {
        long debut = System.currentTimeMillis();
        mockMvc.perform(get("/login")).andExpect(status().isOk());
        assertTempsOk("GET /login", debut);
    }

    @Test
    @Order(3)
    @DisplayName("GET /inscription < 500 ms")
    void inscriptionEstRapide() throws Exception {
        long debut = System.currentTimeMillis();
        mockMvc.perform(get("/inscription")).andExpect(status().isOk());
        assertTempsOk("GET /inscription", debut);
    }

    @Test
    @Order(4)
    @DisplayName("GET /opportunites/public < 500 ms")
    void opportunitesPublicEstRapide() throws Exception {
        long debut = System.currentTimeMillis();
        mockMvc.perform(get("/opportunites/public")).andExpect(status().isOk());
        assertTempsOk("GET /opportunites/public", debut);
    }

    // ── Dashboard Admin ─────────────────────────────────────────────────────

    @Test
    @Order(5)
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /admin/dashboard < 500 ms")
    void adminDashboardEstRapide() throws Exception {
        long debut = System.currentTimeMillis();
        mockMvc.perform(get("/admin/dashboard")).andExpect(status().isOk());
        assertTempsOk("GET /admin/dashboard", debut);
    }

    @Test
    @Order(6)
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /admin/users < 500 ms")
    void adminUsersEstRapide() throws Exception {
        long debut = System.currentTimeMillis();
        mockMvc.perform(get("/admin/users")).andExpect(status().isOk());
        assertTempsOk("GET /admin/users", debut);
    }

    @Test
    @Order(7)
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /admin/clients < 500 ms")
    void adminClientsEstRapide() throws Exception {
        long debut = System.currentTimeMillis();
        mockMvc.perform(get("/admin/clients")).andExpect(status().isOk());
        assertTempsOk("GET /admin/clients", debut);
    }

    // ── Dashboard Commercial ────────────────────────────────────────────────

    @Test
    @Order(8)
    @WithMockUser(username = "commercial@custify.local", roles = "COMMERCIAL")
    @DisplayName("GET /commercial/dashboard < 500 ms")
    void commercialDashboardEstRapide() throws Exception {
        long debut = System.currentTimeMillis();
        mockMvc.perform(get("/commercial/dashboard")).andExpect(status().isOk());
        assertTempsOk("GET /commercial/dashboard", debut);
    }

    @Test
    @Order(9)
    @WithMockUser(username = "commercial@custify.local", roles = "COMMERCIAL")
    @DisplayName("GET /commercial/demandes < 500 ms")
    void commercialDemandesEstRapide() throws Exception {
        long debut = System.currentTimeMillis();
        mockMvc.perform(get("/commercial/demandes")).andExpect(status().isOk());
        assertTempsOk("GET /commercial/demandes", debut);
    }

    @Test
    @Order(10)
    @WithMockUser(username = "commercial@custify.local", roles = "COMMERCIAL")
    @DisplayName("GET /commercial/affectations < 500 ms")
    void commercialAffectationsEstRapide() throws Exception {
        long debut = System.currentTimeMillis();
        mockMvc.perform(get("/commercial/affectations")).andExpect(status().isOk());
        assertTempsOk("GET /commercial/affectations", debut);
    }

    @Test
    @Order(11)
    @WithMockUser(username = "commercial@custify.local", roles = "COMMERCIAL")
    @DisplayName("GET /commercial/clients < 500 ms")
    void commercialClientsEstRapide() throws Exception {
        long debut = System.currentTimeMillis();
        mockMvc.perform(get("/commercial/clients")).andExpect(status().isOk());
        assertTempsOk("GET /commercial/clients", debut);
    }

    @Test
    @Order(12)
    @WithMockUser(username = "commercial@custify.local", roles = "COMMERCIAL")
    @DisplayName("GET /commercial/reunions < 500 ms")
    void commercialReunionsEstRapide() throws Exception {
        long debut = System.currentTimeMillis();
        mockMvc.perform(get("/commercial/reunions")).andExpect(status().isOk());
        assertTempsOk("GET /commercial/reunions", debut);
    }

    // ── Dashboard Client ────────────────────────────────────────────────────

    @Test
    @Order(13)
    @WithMockUser(username = "client@custify.local", roles = "CLIENT")
    @DisplayName("GET /client/dashboard < 500 ms")
    void clientDashboardEstRapide() throws Exception {
        long debut = System.currentTimeMillis();
        mockMvc.perform(get("/client/dashboard")).andExpect(status().isOk());
        assertTempsOk("GET /client/dashboard", debut);
    }

    @Test
    @Order(14)
    @WithMockUser(username = "client@custify.local", roles = "CLIENT")
    @DisplayName("GET /client/opportunites/nouvelle < 500 ms")
    void clientNouvelleOppEstRapide() throws Exception {
        long debut = System.currentTimeMillis();
        mockMvc.perform(get("/client/opportunites/nouvelle")).andExpect(status().isOk());
        assertTempsOk("GET /client/opportunites/nouvelle", debut);
    }

    // ── Helper ──────────────────────────────────────────────────────────────

    private void assertTempsOk(String endpoint, long debut) {
        long elapsed = System.currentTimeMillis() - debut;
        assertThat(elapsed)
                .as("Temps de réponse pour %s : %d ms (seuil %d ms)", endpoint, elapsed, SEUIL_MS)
                .isLessThan(SEUIL_MS);
    }
}
