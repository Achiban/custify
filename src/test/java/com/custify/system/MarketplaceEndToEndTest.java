package com.custify.system;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.custify.dto.CreerOpportuniteMarketplaceRequest;
import com.custify.exception.AccesNonAutoriseException;
import com.custify.model.Affectation;
import com.custify.model.DemandeOpportunite;
import com.custify.model.Opportunite;
import com.custify.model.Utilisateur;
import com.custify.model.enums.Role;
import com.custify.model.enums.StatutAffectation;
import com.custify.model.enums.StatutDemande;
import com.custify.model.enums.StatutOpportunite;
import com.custify.repository.AffectationRepository;
import com.custify.repository.DemandeOpportuniteRepository;
import com.custify.repository.UtilisateurRepository;
import com.custify.service.AffectationService;
import com.custify.service.DemandeService;
import com.custify.service.OpportuniteMarketplaceService;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/**
 * TS-S4 — Tests de bout en bout du flux Marketplace.
 *
 * Flux testé :
 *   Vendeur publie → Acheteur demande → Commercial accepte
 *   → Affectation EN_ATTENTE + Opportunité ATTRIBUEE (correction bug)
 *   → Client accepte → Opportunité CONCLUE
 *   → Client refuse  → Opportunité DISPONIBLE
 *
 * Infrastructure : H2 in-memory, profil "test", transactions rollback après chaque test.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("End-to-End — Flux Marketplace")
class MarketplaceEndToEndTest {

    @Autowired private UtilisateurRepository utilisateurRepository;
    @Autowired private AffectationRepository affectationRepository;
    @Autowired private DemandeOpportuniteRepository demandeRepository;
    @Autowired private OpportuniteMarketplaceService oppService;
    @Autowired private DemandeService demandeService;
    @Autowired private AffectationService affectationService;
    @Autowired private PasswordEncoder passwordEncoder;

    private Utilisateur vendeur;
    private Utilisateur acheteur;
    private Utilisateur commercial;

    @BeforeEach
    void setUp() {
        // Utilisateurs seed créés par DataInitializer
        vendeur    = utilisateurRepository.findByEmail("client@custify.local").orElseThrow();
        commercial = utilisateurRepository.findByEmail("commercial@custify.local").orElseThrow();

        // Second client pour jouer le rôle acheteur (rollback après le test)
        Utilisateur u = new Utilisateur();
        u.setNom("Acheteur");
        u.setPrenom("Test");
        u.setEmail("acheteur.e2e@test.com");
        u.setMotDePasse(passwordEncoder.encode("Test1234!"));
        u.setRole(Role.CLIENT);
        acheteur = utilisateurRepository.save(u);
    }

    private CreerOpportuniteMarketplaceRequest requeteOpp(String titre) {
        CreerOpportuniteMarketplaceRequest r = new CreerOpportuniteMarketplaceRequest();
        r.setTitre(titre);
        r.setDescriptionComplete("Description de test pour " + titre);
        r.setMontant(BigDecimal.valueOf(5000));
        r.setCategorie("IT");
        return r;
    }

    // ── Flux complet ─────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Flux acceptation complète")
    class FluxAcceptation {

        @Test
        @DisplayName("Publication → Demande → Acceptation commercial → Affectation EN_ATTENTE + Opp ATTRIBUEE")
        void accepterDemandeCreeAffectationEnAttenteEtOppAttribuee() {
            Opportunite opp = oppService.publier(requeteOpp("Mission CRM"), vendeur);
            assertThat(opp.getStatut()).isEqualTo(StatutOpportunite.DISPONIBLE);

            DemandeOpportunite demande = demandeService.creerDemande(opp.getId(), acheteur);
            assertThat(demande.getStatut()).isEqualTo(StatutDemande.EN_ATTENTE);

            demandeService.accepterDemande(demande.getId(), commercial);

            // Vérification du statut de la demande
            DemandeOpportunite demandeApres = demandeRepository.findById(demande.getId()).orElseThrow();
            assertThat(demandeApres.getStatut()).isEqualTo(StatutDemande.ACCEPTEE);

            // Opportunité doit être ATTRIBUEE (pas CONCLUE) — correction bug US
            Opportunite oppApres = oppService.trouverParId(opp.getId());
            assertThat(oppApres.getStatut())
                    .as("L'opportunité doit être ATTRIBUEE en attente de confirmation client")
                    .isEqualTo(StatutOpportunite.ATTRIBUEE);

            // Affectation doit être EN_ATTENTE (le client n'a pas encore répondu)
            Affectation affectation = affectationRepository
                    .findByClientBeneficiaire(acheteur).stream().findFirst().orElseThrow();
            assertThat(affectation.getStatutClient())
                    .as("L'affectation doit être EN_ATTENTE de la réponse du client")
                    .isEqualTo(StatutAffectation.EN_ATTENTE);
            assertThat(affectation.getCommercial().getId()).isEqualTo(commercial.getId());
        }

        @Test
        @DisplayName("Client accepte l'affectation → Opportunité CONCLUE")
        void clientAccepteAffectationOppDevientConclue() {
            Opportunite opp = oppService.publier(requeteOpp("Mission ERP"), vendeur);
            demandeService.accepterDemande(
                    demandeService.creerDemande(opp.getId(), acheteur).getId(), commercial);

            Affectation affectation = affectationRepository
                    .findByClientBeneficiaire(acheteur).stream().findFirst().orElseThrow();

            affectationService.accepterAffectation(affectation.getId(), acheteur);

            Affectation affApres = affectationRepository.findById(affectation.getId()).orElseThrow();
            assertThat(affApres.getStatutClient()).isEqualTo(StatutAffectation.ACCEPTEE);

            Opportunite oppApres = oppService.trouverParId(opp.getId());
            assertThat(oppApres.getStatut())
                    .as("L'opportunité doit être CONCLUE après acceptation client")
                    .isEqualTo(StatutOpportunite.CONCLUE);
        }
    }

    @Nested
    @DisplayName("Flux refus")
    class FluxRefus {

        @Test
        @DisplayName("Refus de la demande → Opportunité reste DISPONIBLE")
        void refuserDemandeOpportuniteResteDisponible() {
            Opportunite opp = oppService.publier(requeteOpp("Mission Réseau"), vendeur);
            DemandeOpportunite demande = demandeService.creerDemande(opp.getId(), acheteur);

            demandeService.refuserDemande(demande.getId());

            DemandeOpportunite demandeApres = demandeRepository.findById(demande.getId()).orElseThrow();
            assertThat(demandeApres.getStatut()).isEqualTo(StatutDemande.REFUSEE);

            // L'opportunité doit rester DISPONIBLE (la demande refusée ne la bloque pas)
            assertThat(oppService.trouverParId(opp.getId()).getStatut())
                    .isEqualTo(StatutOpportunite.DISPONIBLE);
        }

        @Test
        @DisplayName("Client refuse l'affectation → Opportunité redevient DISPONIBLE")
        void clientRefuseAffectationOppRedevientDisponible() {
            Opportunite opp = oppService.publier(requeteOpp("Mission Cloud"), vendeur);
            demandeService.accepterDemande(
                    demandeService.creerDemande(opp.getId(), acheteur).getId(), commercial);

            Affectation affectation = affectationRepository
                    .findByClientBeneficiaire(acheteur).stream().findFirst().orElseThrow();

            affectationService.refuserAffectation(affectation.getId(), acheteur);

            assertThat(affectationRepository.findById(affectation.getId()).orElseThrow().getStatutClient())
                    .isEqualTo(StatutAffectation.REFUSEE);

            assertThat(oppService.trouverParId(opp.getId()).getStatut())
                    .as("L'opportunité doit redevenir DISPONIBLE après refus client")
                    .isEqualTo(StatutOpportunite.DISPONIBLE);
        }
    }

    // ── Gardes métier ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Gardes métier — cas d'erreur")
    class GardesMetier {

        @Test
        @DisplayName("Vendeur ne peut pas demander sa propre opportunité")
        void vendeurNePeutPasDemanderSaPropre() {
            Opportunite opp = oppService.publier(requeteOpp("Mission Sécurité"), vendeur);

            assertThatThrownBy(() -> demandeService.creerDemande(opp.getId(), vendeur))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("propre");
        }

        @Test
        @DisplayName("Double demande par le même acheteur → rejetée")
        void doubleDemandeMemeAcheteur() {
            Opportunite opp = oppService.publier(requeteOpp("Mission Data"), vendeur);
            demandeService.creerDemande(opp.getId(), acheteur);

            assertThatThrownBy(() -> demandeService.creerDemande(opp.getId(), acheteur))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("déjà");
        }

        @Test
        @DisplayName("Demande sur opportunité non DISPONIBLE → rejetée")
        void demandeOppNonDisponible() {
            Opportunite opp = oppService.publier(requeteOpp("Mission DevOps"), vendeur);
            demandeService.accepterDemande(
                    demandeService.creerDemande(opp.getId(), acheteur).getId(), commercial);
            // opp est maintenant ATTRIBUEE

            Utilisateur autreAcheteur = new Utilisateur();
            autreAcheteur.setNom("Autre");
            autreAcheteur.setPrenom("Acheteur");
            autreAcheteur.setEmail("autre.acheteur@test.com");
            autreAcheteur.setMotDePasse(passwordEncoder.encode("Test1234!"));
            autreAcheteur.setRole(Role.CLIENT);
            autreAcheteur = utilisateurRepository.save(autreAcheteur);
            final Utilisateur autreAcheteurFinal = autreAcheteur;

            assertThatThrownBy(() -> demandeService.creerDemande(opp.getId(), autreAcheteurFinal))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("disponible");
        }

        @Test
        @DisplayName("Modifier une opportunité non DISPONIBLE → rejetée")
        void modifierOppNonDisponible() {
            Opportunite opp = oppService.publier(requeteOpp("Mission Réseau"), vendeur);
            demandeService.accepterDemande(
                    demandeService.creerDemande(opp.getId(), acheteur).getId(), commercial);
            // opp est maintenant ATTRIBUEE

            assertThatThrownBy(() -> oppService.modifier(opp.getId(), requeteOpp("Nouveau titre"), vendeur))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("Supprimer l'opportunité d'un autre vendeur → AccesNonAutorise")
        void supprimerOppAutreVendeur() {
            Opportunite opp = oppService.publier(requeteOpp("Mission UX"), vendeur);

            assertThatThrownBy(() -> oppService.supprimer(opp.getId(), acheteur))
                    .isInstanceOf(AccesNonAutoriseException.class);
        }

        @Test
        @DisplayName("Accepter une demande déjà traitée → rejetée")
        void accepterDemandeDejaTraitee() {
            Opportunite opp = oppService.publier(requeteOpp("Mission BI"), vendeur);
            DemandeOpportunite demande = demandeService.creerDemande(opp.getId(), acheteur);
            demandeService.refuserDemande(demande.getId());

            assertThatThrownBy(() -> demandeService.accepterDemande(demande.getId(), commercial))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("traitée");
        }

        @Test
        @DisplayName("Client ne peut pas répondre à l'affectation d'un autre client")
        void clientNePeutRepondreAffectationAutre() {
            Opportunite opp = oppService.publier(requeteOpp("Mission QA"), vendeur);
            demandeService.accepterDemande(
                    demandeService.creerDemande(opp.getId(), acheteur).getId(), commercial);

            Affectation affectation = affectationRepository
                    .findByClientBeneficiaire(acheteur).stream().findFirst().orElseThrow();

            // Le vendeur (pas le bénéficiaire) tente d'accepter
            assertThatThrownBy(() -> affectationService.accepterAffectation(affectation.getId(), vendeur))
                    .isInstanceOf(AccesNonAutoriseException.class);
        }
    }

    // ── Affectation directe par le commercial ────────────────────────────────

    @Nested
    @DisplayName("Affectation directe par le commercial")
    class AffectationDirecte {

        @Test
        @DisplayName("Commercial crée une affectation directe → EN_ATTENTE + Opp ATTRIBUEE")
        void affectationDirecteEnAttenteEtOppAttribuee() {
            Opportunite opp = oppService.publier(requeteOpp("Mission Analytics"), vendeur);

            com.custify.dto.CreerAffectationRequest req = new com.custify.dto.CreerAffectationRequest();
            req.setClientBeneficiaireId(acheteur.getId());
            req.setOpportuniteId(opp.getId());
            req.setMessageCommercial("Je vous propose cette opportunité.");

            Affectation affectation = affectationService.creerAffectation(req, commercial);

            assertThat(affectation.getStatutClient()).isEqualTo(StatutAffectation.EN_ATTENTE);
            assertThat(oppService.trouverParId(opp.getId()).getStatut())
                    .isEqualTo(StatutOpportunite.ATTRIBUEE);
        }

        @Test
        @DisplayName("Affectation directe sur opportunité non DISPONIBLE → rejetée")
        void affectationDirecteOppNonDisponible() {
            Opportunite opp = oppService.publier(requeteOpp("Mission Infra"), vendeur);
            // Première affectation → ATTRIBUEE
            com.custify.dto.CreerAffectationRequest req1 = new com.custify.dto.CreerAffectationRequest();
            req1.setClientBeneficiaireId(acheteur.getId());
            req1.setOpportuniteId(opp.getId());
            req1.setMessageCommercial("Première affectation");
            affectationService.creerAffectation(req1, commercial);

            // Deuxième tentative sur la même opp (ATTRIBUEE) → exception
            com.custify.dto.CreerAffectationRequest req2 = new com.custify.dto.CreerAffectationRequest();
            req2.setClientBeneficiaireId(acheteur.getId());
            req2.setOpportuniteId(opp.getId());
            req2.setMessageCommercial("Deuxième tentative");

            assertThatThrownBy(() -> affectationService.creerAffectation(req2, commercial))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("DISPONIBLE");
        }
    }
}
