package com.custify.repository;

import com.custify.model.Opportunite;
import com.custify.model.enums.StatutOpportunite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface OpportuniteRepository extends JpaRepository<Opportunite, Long> {

    List<Opportunite> findByClientUtilisateurId(Long userId);

    List<Opportunite> findByStatut(StatutOpportunite statut);

    List<Opportunite> findByClientUtilisateurIdAndStatut(Long userId, StatutOpportunite statut);

    List<Opportunite> findByStatutIn(List<StatutOpportunite> statuts);

    List<Opportunite> findByClientUtilisateurIdAndStatutIn(Long userId, List<StatutOpportunite> statuts);

    @Query("SELECT SUM(o.montant) FROM Opportunite o WHERE o.statut = :statut")
    BigDecimal sumMontantByStatut(StatutOpportunite statut);

    @Query("SELECT SUM(o.montant) FROM Opportunite o WHERE o.client.utilisateur.id = :userId AND o.statut = :statut")
    BigDecimal sumMontantByClientUtilisateurIdAndStatut(Long userId, StatutOpportunite statut);
}