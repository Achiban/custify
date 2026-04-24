package com.custify.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.custify.model.Client;
import java.util.List;

public interface ClientRepository extends JpaRepository<Client, Long> {

    List<Client> findByUtilisateurId(Long id);

    boolean existsByEmail(String email);
}