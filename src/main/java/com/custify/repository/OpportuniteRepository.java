package com.custify.repository;

import com.custify.model.Opportunite;
import com.custify.model.Utilisateur;
import com.custify.model.enums.StatutOpportunite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OpportuniteRepository extends JpaRepository<Opportunite, Long> {

    List<Opportunite> findByStatut(StatutOpportunite statut);

    List<Opportunite> findByClientVendeur(Utilisateur vendeur);

    List<Opportunite> findByClientVendeurAndStatut(Utilisateur vendeur, StatutOpportunite statut);

    List<Opportunite> findByStatutNot(StatutOpportunite statut);
}