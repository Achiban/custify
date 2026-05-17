package com.custify.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.custify.dto.CreerAffectationRequest;
import com.custify.exception.AccesNonAutoriseException;
import com.custify.model.Affectation;
import com.custify.model.Opportunite;
import com.custify.model.Utilisateur;
import com.custify.model.enums.Role;
import com.custify.model.enums.StatutAffectation;
import com.custify.model.enums.StatutOpportunite;
import com.custify.repository.AffectationRepository;
import com.custify.repository.OpportuniteRepository;
import com.custify.repository.UtilisateurRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AffectationServiceTest {

    @Mock private AffectationRepository affectationRepository;
    @Mock private OpportuniteRepository opportuniteRepository;
    @Mock private UtilisateurRepository utilisateurRepository;

    private AffectationService service;

    @BeforeEach
    void setUp() {
        service = new AffectationService(affectationRepository, opportuniteRepository, utilisateurRepository);
    }

    @Test
    void creerAffectationShouldCreateAndMarkOpportuniteAttribuee() {
        Utilisateur commercial = utilisateur(1L, Role.COMMERCIAL);
        Utilisateur client = utilisateur(2L, Role.CLIENT);
        Opportunite opp = opportunite(10L, StatutOpportunite.DISPONIBLE, utilisateur(3L, Role.CLIENT));

        CreerAffectationRequest req = new CreerAffectationRequest();
        req.setClientBeneficiaireId(2L);
        req.setOpportuniteId(10L);
        req.setMessageCommercial("Bonjour");

        when(utilisateurRepository.findById(2L)).thenReturn(Optional.of(client));
        when(opportuniteRepository.findById(10L)).thenReturn(Optional.of(opp));
        when(opportuniteRepository.save(any())).thenReturn(opp);
        when(affectationRepository.save(any())).thenAnswer(inv -> {
            Affectation a = inv.getArgument(0);
            a.setId(100L);
            return a;
        });

        Affectation result = service.creerAffectation(req, commercial);

        assertEquals(StatutAffectation.EN_ATTENTE, result.getStatutClient());
        assertEquals(commercial, result.getCommercial());
        assertEquals(client, result.getClientBeneficiaire());

        ArgumentCaptor<Opportunite> captor = ArgumentCaptor.forClass(Opportunite.class);
        verify(opportuniteRepository).save(captor.capture());
        assertEquals(StatutOpportunite.ATTRIBUEE, captor.getValue().getStatut());
    }

    @Test
    void creerAffectationShouldThrowWhenClientNotFound() {
        CreerAffectationRequest req = new CreerAffectationRequest();
        req.setClientBeneficiaireId(99L);
        req.setOpportuniteId(1L);

        when(utilisateurRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> service.creerAffectation(req, utilisateur(1L, Role.COMMERCIAL)));
    }

    @Test
    void creerAffectationShouldThrowWhenBeneficiaireIsNotClient() {
        Utilisateur commercial = utilisateur(1L, Role.COMMERCIAL);
        Utilisateur notClient = utilisateur(2L, Role.COMMERCIAL);

        CreerAffectationRequest req = new CreerAffectationRequest();
        req.setClientBeneficiaireId(2L);
        req.setOpportuniteId(10L);

        when(utilisateurRepository.findById(2L)).thenReturn(Optional.of(notClient));

        assertThrows(IllegalArgumentException.class, () -> service.creerAffectation(req, commercial));
    }

    @Test
    void creerAffectationShouldThrowWhenOpportuniteNotDisponible() {
        Utilisateur commercial = utilisateur(1L, Role.COMMERCIAL);
        Utilisateur client = utilisateur(2L, Role.CLIENT);
        Opportunite opp = opportunite(10L, StatutOpportunite.ATTRIBUEE, utilisateur(3L, Role.CLIENT));

        CreerAffectationRequest req = new CreerAffectationRequest();
        req.setClientBeneficiaireId(2L);
        req.setOpportuniteId(10L);

        when(utilisateurRepository.findById(2L)).thenReturn(Optional.of(client));
        when(opportuniteRepository.findById(10L)).thenReturn(Optional.of(opp));

        assertThrows(IllegalStateException.class, () -> service.creerAffectation(req, commercial));
    }

    @Test
    void accepterAffectationShouldSetAccepteeAndMarkOpportuniteConclue() {
        Utilisateur client = utilisateur(2L, Role.CLIENT);
        Opportunite opp = opportunite(10L, StatutOpportunite.ATTRIBUEE, utilisateur(3L, Role.CLIENT));
        Affectation affectation = affectation(50L, client, opp, StatutAffectation.EN_ATTENTE);

        when(affectationRepository.findById(50L)).thenReturn(Optional.of(affectation));
        when(affectationRepository.save(any())).thenReturn(affectation);
        when(opportuniteRepository.save(any())).thenReturn(opp);

        service.accepterAffectation(50L, client);

        assertEquals(StatutAffectation.ACCEPTEE, affectation.getStatutClient());
        assertEquals(StatutOpportunite.CONCLUE, opp.getStatut());
        verify(affectationRepository).save(affectation);
        verify(opportuniteRepository).save(opp);
    }

    @Test
    void accepterAffectationShouldThrowWhenNotBeneficiaire() {
        Utilisateur client = utilisateur(2L, Role.CLIENT);
        Utilisateur autre = utilisateur(3L, Role.CLIENT);
        Opportunite opp = opportunite(10L, StatutOpportunite.ATTRIBUEE, utilisateur(4L, Role.CLIENT));
        Affectation affectation = affectation(50L, client, opp, StatutAffectation.EN_ATTENTE);

        when(affectationRepository.findById(50L)).thenReturn(Optional.of(affectation));

        assertThrows(AccesNonAutoriseException.class, () -> service.accepterAffectation(50L, autre));
    }

    @Test
    void accepterAffectationShouldThrowWhenAlreadyTreated() {
        Utilisateur client = utilisateur(2L, Role.CLIENT);
        Opportunite opp = opportunite(10L, StatutOpportunite.ATTRIBUEE, utilisateur(3L, Role.CLIENT));
        Affectation affectation = affectation(50L, client, opp, StatutAffectation.ACCEPTEE);

        when(affectationRepository.findById(50L)).thenReturn(Optional.of(affectation));

        assertThrows(IllegalStateException.class, () -> service.accepterAffectation(50L, client));
    }

    @Test
    void refuserAffectationShouldSetRefuseeAndMarkOpportuniteDisponible() {
        Utilisateur client = utilisateur(2L, Role.CLIENT);
        Opportunite opp = opportunite(10L, StatutOpportunite.ATTRIBUEE, utilisateur(3L, Role.CLIENT));
        Affectation affectation = affectation(50L, client, opp, StatutAffectation.EN_ATTENTE);

        when(affectationRepository.findById(50L)).thenReturn(Optional.of(affectation));
        when(affectationRepository.save(any())).thenReturn(affectation);
        when(opportuniteRepository.save(any())).thenReturn(opp);

        service.refuserAffectation(50L, client);

        assertEquals(StatutAffectation.REFUSEE, affectation.getStatutClient());
        assertEquals(StatutOpportunite.DISPONIBLE, opp.getStatut());
    }

    @Test
    void listerParClientShouldReturnAffectations() {
        Utilisateur client = utilisateur(2L, Role.CLIENT);
        Affectation a = new Affectation();
        when(affectationRepository.findByClientBeneficiaire(client)).thenReturn(List.of(a));

        List<Affectation> result = service.listerParClient(client);

        assertEquals(1, result.size());
    }

    @Test
    void listerParCommercialShouldReturnAffectations() {
        Utilisateur commercial = utilisateur(1L, Role.COMMERCIAL);
        Affectation a = new Affectation();
        when(affectationRepository.findByCommercial(commercial)).thenReturn(List.of(a));

        List<Affectation> result = service.listerParCommercial(commercial);

        assertEquals(1, result.size());
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private Utilisateur utilisateur(Long id, Role role) {
        Utilisateur u = new Utilisateur();
        u.setId(id);
        u.setRole(role);
        return u;
    }

    private Opportunite opportunite(Long id, StatutOpportunite statut, Utilisateur vendeur) {
        Opportunite o = new Opportunite();
        o.setId(id);
        o.setStatut(statut);
        o.setClientVendeur(vendeur);
        o.setTitre("T");
        o.setDescriptionComplete("D");
        o.setMontant(BigDecimal.TEN);
        return o;
    }

    private Affectation affectation(Long id, Utilisateur client, Opportunite opp, StatutAffectation statut) {
        Affectation a = new Affectation();
        a.setId(id);
        a.setClientBeneficiaire(client);
        a.setOpportunite(opp);
        a.setStatutClient(statut);
        return a;
    }
}
