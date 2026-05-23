package com.opsforge.backend.repositories;

import com.opsforge.backend.models.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
    // Inherits all standard CRUD operations (save, findById, findAll, deleteById)
}