package com.custify.repository;

import com.custify.model.Affectation;
import com.custify.model.Utilisateur;
import com.custify.model.enums.StatutAffectation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AffectationRepository extends JpaRepository<Affectation, Long> {

    List<Affectation> findByClientBeneficiaire(Utilisateur client);

    List<Affectation> findByClientBeneficiaireAndStatutClient(Utilisateur client, StatutAffectation statut);

    List<Affectation> findByCommercial(Utilisateur commercial);

    List<Affectation> findByStatutClient(StatutAffectation statut);
}
