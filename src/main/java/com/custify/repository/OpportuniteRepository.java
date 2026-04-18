package com.custify.repository;

import com.custify.model.Opportunite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OpportuniteRepository extends JpaRepository<Opportunite, Long> {
}
