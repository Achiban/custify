package com.custify.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.custify.model.Interaction;

public interface InteractionRepository extends JpaRepository<Interaction, Long> {

    List<Interaction> findByClientId(Long clientId);
    
    List<Interaction> findByUtilisateurId(Long utilisateurId);
    
    List<Interaction> findAll();
}