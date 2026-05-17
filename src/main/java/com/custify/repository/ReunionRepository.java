package com.custify.repository;

import com.custify.model.Reunion;
import com.custify.model.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReunionRepository extends JpaRepository<Reunion, Long> {
    List<Reunion> findByCommercial(Utilisateur commercial);
    List<Reunion> findByClientVendeurOrClientAcheteur(Utilisateur vendeur, Utilisateur acheteur);
}
