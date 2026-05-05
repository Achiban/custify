package com.custify.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.custify.model.Prospect;

@Repository
public interface ProspectRepository extends JpaRepository<Prospect, Long> {
    boolean existsByEmail(String email);
}
