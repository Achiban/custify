package com.custify.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.custify.model.Affectation;
import com.custify.model.Opportunite;
import com.custify.model.Reunion;
import com.custify.model.Utilisateur;
import com.custify.model.enums.StatutOpportunite;
import com.custify.repository.AffectationRepository;
import com.custify.repository.ReunionRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReunionServiceTest {

    @Mock private ReunionRepository reunionRepository;
    @Mock private AffectationRepository affectationRepository;

    private ReunionService service;

    @BeforeEach
    void setUp() {
        service = new ReunionService(reunionRepository, affectationRepository);
    }

    @Test
    void organiserReunionShouldCreateReunionWithAllParticipants() {
        Utilisateur commercial = utilisateur(1L);
        Utilisateur acheteur = utilisateur(2L);
        Utilisateur vendeur = utilisateur(3L);
        Opportunite opp = opportunite(10L, vendeur);
        Affectation affectation = affectation(50L, commercial, acheteur, opp);

        when(affectationRepository.findById(50L)).thenReturn(Optional.of(affectation));
        when(reunionRepository.save(any())).thenAnswer(inv -> {
            Reunion r = inv.getArgument(0);
            r.setId(99L);
            return r;
        });

        LocalDateTime date = LocalDateTime.of(2025, 6, 1, 10, 0);
        Reunion result = service.organiserReunion(50L, date, "Sujet", "Paris", "Description", commercial);

        assertEquals(commercial, result.getCommercial());
        assertEquals(vendeur, result.getClientVendeur());
        assertEquals(acheteur, result.getClientAcheteur());
        assertEquals(opp, result.getOpportunite());
        assertEquals(date, result.getDateReunion());

        ArgumentCaptor<Reunion> captor = ArgumentCaptor.forClass(Reunion.class);
        verify(reunionRepository).save(captor.capture());
        assertEquals("Sujet", captor.getValue().getSujet());
        assertEquals("Paris", captor.getValue().getLieu());
    }

    @Test
    void organiserReunionShouldThrowWhenAffectationNotFound() {
        when(affectationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> service.organiserReunion(99L, LocalDateTime.now(), "S", "L", "D", utilisateur(1L)));
    }

    @Test
    void listerParCommercialShouldReturnReunions() {
        Utilisateur commercial = utilisateur(1L);
        Reunion r = new Reunion();
        when(reunionRepository.findByCommercial(commercial)).thenReturn(List.of(r));

        List<Reunion> result = service.listerParCommercial(commercial);

        assertEquals(1, result.size());
    }

    @Test
    void listerParClientShouldReturnReunionsWhenVendeurOrAcheteur() {
        Utilisateur client = utilisateur(2L);
        Reunion r = new Reunion();
        when(reunionRepository.findByClientVendeurOrClientAcheteur(client, client)).thenReturn(List.of(r));

        List<Reunion> result = service.listerParClient(client);

        assertEquals(1, result.size());
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private Utilisateur utilisateur(Long id) {
        Utilisateur u = new Utilisateur();
        u.setId(id);
        return u;
    }

    private Opportunite opportunite(Long id, Utilisateur vendeur) {
        Opportunite o = new Opportunite();
        o.setId(id);
        o.setClientVendeur(vendeur);
        o.setStatut(StatutOpportunite.ATTRIBUEE);
        o.setTitre("T");
        o.setDescriptionComplete("D");
        o.setMontant(BigDecimal.TEN);
        return o;
    }

    private Affectation affectation(Long id, Utilisateur commercial, Utilisateur client, Opportunite opp) {
        Affectation a = new Affectation();
        a.setId(id);
        a.setCommercial(commercial);
        a.setClientBeneficiaire(client);
        a.setOpportunite(opp);
        return a;
    }
}
