package com.custify.repository;

import com.custify.model.MessageOpportunite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageOpportuniteRepository extends JpaRepository<MessageOpportunite, Long> {
    Page<MessageOpportunite> findByConversationIdOrderByCreatedAtDesc(Long conversationId, Pageable pageable);
}
