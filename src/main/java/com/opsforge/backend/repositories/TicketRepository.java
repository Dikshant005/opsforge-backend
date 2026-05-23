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
    List<Ticket> findByDeveloperAndIsDeletedFalse(User developer);
    List<Ticket> findByStatusAndDeveloperAndIsDeletedFalse(String status, User developer);
}