package com.custify.repository;

import com.custify.model.DemandeOpportunite;
import com.custify.model.Opportunite;
import com.custify.model.Utilisateur;
import com.custify.model.enums.StatutDemande;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DemandeOpportuniteRepository extends JpaRepository<DemandeOpportunite, Long> {

    List<DemandeOpportunite> findByClientDemandeur(Utilisateur client);

    List<DemandeOpportunite> findByStatut(StatutDemande statut);

    List<DemandeOpportunite> findByOpportunite(Opportunite opportunite);

    boolean existsByClientDemandeurAndOpportunite(Utilisateur client, Opportunite opportunite);
}
