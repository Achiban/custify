package com.custify.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.custify.model.Client;

public interface ClientRepository extends JpaRepository<Client, Long> {

    List<Client> findByUtilisateurId(Long id);

    boolean existsByEmail(String email);
    
    // Search by nom (name) for a specific user
    List<Client> findByUtilisateurIdAndNomContainingIgnoreCase(Long userId, String nom);
    
    // Search by email for a specific user
    List<Client> findByUtilisateurIdAndEmailContainingIgnoreCase(Long userId, String email);
    
    // Search by entreprise (company) for a specific user
    List<Client> findByUtilisateurIdAndEntrepriseContainingIgnoreCase(Long userId, String entreprise);
    
    // Search by telephone (phone) for a specific user
    List<Client> findByUtilisateurIdAndTelephoneContainingIgnoreCase(Long userId, String telephone);
    
    // Advanced search: multiple criteria
    @Query("SELECT c FROM Client c WHERE c.utilisateur.id = :userId AND " +
           "(LOWER(c.nom) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.entreprise) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.telephone) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<Client> searchClients(@Param("userId") Long userId, @Param("searchTerm") String searchTerm);
}