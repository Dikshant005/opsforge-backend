package com.opsforge.backend.repositories;

import com.opsforge.backend.models.Ticket;
import com.opsforge.backend.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findByIsDeletedFalse();
    List<Ticket> findByStatusAndIsDeletedFalse(String status);
    List<Ticket> findByDevelopersContainingAndIsDeletedFalse(User developer);
    List<Ticket> findByStatusAndDevelopersContainingAndIsDeletedFalse(String status, User developer);
    long countByStatusAndIsDeletedFalse(String status);
    
    // Dashboard queries
    long countByDevelopersContainingAndStatusInAndIsDeletedFalse(User developer, List<String> statuses);
    List<Ticket> findTop5ByDevelopersContainingAndStatusInAndIsDeletedFalseOrderByCreatedAtDesc(User developer, List<String> statuses);
    
    long countByReviewersContainingAndStatusAndIsDeletedFalse(User reviewer, String status);
    List<Ticket> findTop5ByStatusAndIsDeletedFalseOrderByCreatedAtDesc(String status);
    List<Ticket> findTop5ByReviewersContainingAndStatusAndIsDeletedFalseOrderByCreatedAtDesc(User reviewer, String status);
    
    long countByIsDeletedFalse();
    long countByStatusNotAndIsDeletedFalse(String status);
}