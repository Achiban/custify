package com.custify.system;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/**
 * TS-S4 — Vérifications de sécurité : contrôle d'accès par rôle et protection CSRF.
 *
 * Stratégie :
 *  - Anonymes → 302 vers /login sur toute ressource protégée
 *  - Mauvais rôle → 403 (AccessDeniedHandlerImpl forward vers /access-denied)
 *  - POST sans jeton CSRF → 403
 *  - Endpoints publics → 200 sans authentification
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Sécurité — Contrôle d'accès et CSRF")
class SecurityAccessControlTest {

    @Autowired
    private MockMvc mockMvc;

    // ── Endpoints publics ───────────────────────────────────────────────────

    @Nested
    @DisplayName("Endpoints publics accessibles sans authentification")
    class PublicEndpoints {

        @Test
        @DisplayName("GET / → 200")
        void indexEstPublic() throws Exception {
            mockMvc.perform(get("/"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("GET /login → 200")
        void loginEstPublic() throws Exception {
            mockMvc.perform(get("/login"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("GET /inscription → 200")
        void inscriptionEstPublique() throws Exception {
            mockMvc.perform(get("/inscription"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("GET /opportunites/public → 200")
        void opportunitesPubliquesEstPublic() throws Exception {
            mockMvc.perform(get("/opportunites/public"))
                    .andExpect(status().isOk());
        }
    }

    // ── Utilisateurs anonymes redirigés vers /login ─────────────────────────

    @Nested
    @DisplayName("Ressources protégées → redirection vers /login si non authentifié")
    class RedirectionAnonymous {

        @Test
        @DisplayName("GET /admin/dashboard redirige vers /login")
        void adminDashboardRequiertAuth() throws Exception {
            mockMvc.perform(get("/admin/dashboard"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrlPattern("**/login"));
        }

        @Test
        @DisplayName("GET /commercial/dashboard redirige vers /login")
        void commercialDashboardRequiertAuth() throws Exception {
            mockMvc.perform(get("/commercial/dashboard"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrlPattern("**/login"));
        }

        @Test
        @DisplayName("GET /client/dashboard redirige vers /login")
        void clientDashboardRequiertAuth() throws Exception {
            mockMvc.perform(get("/client/dashboard"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrlPattern("**/login"));
        }

        @Test
        @DisplayName("GET /admin/users redirige vers /login")
        void adminUsersRequiertAuth() throws Exception {
            mockMvc.perform(get("/admin/users"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrlPattern("**/login"));
        }
    }

    // ── Cloisonnement des rôles ─────────────────────────────────────────────

    @Nested
    @DisplayName("Cloisonnement des rôles — accès refusé hors périmètre")
    class RoleEnforcement {

        @Test
        @WithMockUser(roles = "CLIENT")
        @DisplayName("CLIENT ne peut pas accéder à /admin/dashboard → 403")
        void clientNePeutPasAccederAdmin() throws Exception {
            mockMvc.perform(get("/admin/dashboard"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "CLIENT")
        @DisplayName("CLIENT ne peut pas accéder à /commercial/dashboard → 403")
        void clientNePeutPasAccederCommercial() throws Exception {
            mockMvc.perform(get("/commercial/dashboard"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "COMMERCIAL")
        @DisplayName("COMMERCIAL ne peut pas accéder à /admin/dashboard → 403")
        void commercialNePeutPasAccederAdmin() throws Exception {
            mockMvc.perform(get("/admin/dashboard"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "COMMERCIAL")
        @DisplayName("COMMERCIAL ne peut pas accéder à /client/dashboard → 403")
        void commercialNePeutPasAccederClient() throws Exception {
            mockMvc.perform(get("/client/dashboard"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("ADMIN ne peut pas accéder à /client/dashboard → 403")
        void adminNePeutPasAccederClient() throws Exception {
            mockMvc.perform(get("/client/dashboard"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("ADMIN ne peut pas accéder à /commercial/dashboard → 403")
        void adminNePeutPasAccederCommercial() throws Exception {
            mockMvc.perform(get("/commercial/dashboard"))
                    .andExpect(status().isForbidden());
        }
    }

    // ── Protection CSRF ─────────────────────────────────────────────────────

    @Nested
    @DisplayName("Protection CSRF")
    class CsrfProtection {

        @Test
        @DisplayName("POST sans jeton CSRF → 403")
        void postSansCsrfEstRefuse() throws Exception {
            mockMvc.perform(post("/inscription")
                            .param("nom", "Test")
                            .param("prenom", "User")
                            .param("email", "test@test.com")
                            .param("motDePasse", "Password1!"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("POST avec jeton CSRF valide → traité (pas 403)")
        void postAvecCsrfEstAccepte() throws Exception {
            mockMvc.perform(post("/inscription").with(csrf())
                            .param("nom", "Test")
                            .param("prenom", "User")
                            .param("email", "csrf.test@example.com")
                            .param("motDePasse", "Password1!")
                            .param("telephone", "0600000000"))
                    .andExpect(status().is3xxRedirection()); // redirect /login après inscription
        }

        @Test
        @WithMockUser(roles = "CLIENT")
        @DisplayName("POST /logout sans CSRF → 403")
        void logoutSansCsrfEstRefuse() throws Exception {
            mockMvc.perform(post("/logout"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "CLIENT")
        @DisplayName("POST /logout avec CSRF → redirection")
        void logoutAvecCsrfRedirige() throws Exception {
            mockMvc.perform(post("/logout").with(csrf()))
                    .andExpect(status().is3xxRedirection());
        }
    }

    // ── Accès autorisé (happy path) ─────────────────────────────────────────

    @Nested
    @DisplayName("Accès autorisé — rôle correct")
    class AccesAutorise {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("ADMIN peut accéder à /admin/dashboard → 200")
        void adminAccedeAdminDashboard() throws Exception {
            mockMvc.perform(get("/admin/dashboard"))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("ADMIN peut accéder à /admin/users → 200")
        void adminAccedeAdminUsers() throws Exception {
            mockMvc.perform(get("/admin/users"))
                    .andExpect(status().isOk());
        }
    }
}
