package com.custify.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.custify.repository.ClientRepository;
import com.custify.repository.InteractionRepository;
import com.custify.repository.OpportuniteRepository;
import com.custify.repository.ProspectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private ProspectRepository prospectRepository;

    @Mock
    private OpportuniteRepository opportuniteRepository;

    @Mock
    private InteractionRepository interactionRepository;

    private DashboardService dashboardService;

    @BeforeEach
    void setUp() {
        dashboardService = new DashboardService(
                clientRepository,
                prospectRepository,
                opportuniteRepository,
                interactionRepository);
    }

    @Test
    void getStatsShouldAggregateCountsFromAllRepositories() {
        when(clientRepository.count()).thenReturn(5L);
        when(prospectRepository.count()).thenReturn(3L);
        when(opportuniteRepository.count()).thenReturn(2L);
        when(interactionRepository.count()).thenReturn(11L);

        DashboardService.DashboardStats stats = dashboardService.getStats();

        assertEquals(5L, stats.getClientCount());
        assertEquals(3L, stats.getProspectCount());
        assertEquals(2L, stats.getOpportuniteCount());
        assertEquals(11L, stats.getInteractionCount());
    }
}
