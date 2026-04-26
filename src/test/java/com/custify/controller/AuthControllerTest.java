package com.custify.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.custify.service.DashboardService;
import com.custify.service.DashboardService.DashboardStats;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ExtendedModelMap;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private DashboardService dashboardService;

    private AuthController authController;

    @BeforeEach
    void setUp() {
        authController = new AuthController(dashboardService);
    }

    @Test
    void showLoginPageShouldExposeErrorAndLogoutMessages() {
        ExtendedModelMap model = new ExtendedModelMap();

        String viewName = authController.showLoginPage("true", "true", model);

        assertEquals("login", viewName);
        assertEquals("Invalid email or password", model.getAttribute("error"));
        assertEquals("You have been logged out successfully", model.getAttribute("message"));
    }

    @Test
    void homeShouldRedirectToDashboard() {
        assertEquals("redirect:/dashboard", authController.home());
    }

    @Test
    void dashboardShouldAddStatsToModel() {
        DashboardStats stats = new DashboardStats(1L, 2L, 3L, 4L);
        when(dashboardService.getStats()).thenReturn(stats);
        ExtendedModelMap model = new ExtendedModelMap();

        String viewName = authController.dashboard(model);

        assertEquals("dashboard", viewName);
        assertEquals(stats, model.getAttribute("stats"));
        verify(dashboardService).getStats();
    }

    @Test
    void accessDeniedShouldReturnAccessDeniedView() {
        assertEquals("access-denied", authController.accessDenied());
    }
}
