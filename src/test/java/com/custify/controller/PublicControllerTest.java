package com.custify.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.custify.model.Opportunite;
import com.custify.model.enums.StatutOpportunite;
import com.custify.repository.OpportuniteRepository;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ExtendedModelMap;

@ExtendWith(MockitoExtension.class)
class PublicControllerTest {

    @Mock
    private OpportuniteRepository opportuniteRepository;

    private PublicController controller;

    @BeforeEach
    void setUp() {
        controller = new PublicController(opportuniteRepository);
    }

    @Test
    void indexShouldReturnIndexViewWithDisponibleOpportunites() {
        Opportunite opp = opportunite(1L);
        when(opportuniteRepository.findByStatut(StatutOpportunite.DISPONIBLE)).thenReturn(List.of(opp));

        ExtendedModelMap model = new ExtendedModelMap();
        String view = controller.index(model);

        assertEquals("index", view);
        List<?> opportunites = (List<?>) model.getAttribute("opportunites");
        assertEquals(1, opportunites.size());
    }

    @Test
    void publicOpportunitesShouldReturnIndexViewWithDisponibleOpportunites() {
        Opportunite opp = opportunite(2L);
        when(opportuniteRepository.findByStatut(StatutOpportunite.DISPONIBLE)).thenReturn(List.of(opp));

        ExtendedModelMap model = new ExtendedModelMap();
        String view = controller.publicOpportunites(model);

        assertEquals("index", view);
        List<?> opportunites = (List<?>) model.getAttribute("opportunites");
        assertEquals(1, opportunites.size());
    }

    @Test
    void indexShouldReturnEmptyListWhenNoDisponibleOpportunites() {
        when(opportuniteRepository.findByStatut(StatutOpportunite.DISPONIBLE)).thenReturn(List.of());

        ExtendedModelMap model = new ExtendedModelMap();
        controller.index(model);

        List<?> opportunites = (List<?>) model.getAttribute("opportunites");
        assertEquals(0, opportunites.size());
    }

    private Opportunite opportunite(Long id) {
        Opportunite o = new Opportunite();
        o.setId(id);
        o.setStatut(StatutOpportunite.DISPONIBLE);
        o.setTitre("Opportunite " + id);
        o.setDescriptionComplete("Description");
        o.setMontant(BigDecimal.valueOf(1000));
        return o;
    }
}
