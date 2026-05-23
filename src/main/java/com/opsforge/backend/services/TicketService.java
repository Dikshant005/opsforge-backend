package com.opsforge.backend.services;

import com.opsforge.backend.models.AuditLog;
import com.opsforge.backend.models.Ticket;
import com.opsforge.backend.models.User;
import com.opsforge.backend.repositories.AuditLogRepository;
import com.opsforge.backend.repositories.TicketRepository;
import com.opsforge.backend.repositories.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TicketService {

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;

    public TicketService(TicketRepository ticketRepository, UserRepository userRepository, AuditLogRepository auditLogRepository) {
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.auditLogRepository = auditLogRepository;
    }

    // Create a Ticket
    @Transactional
    public Ticket createTicket(String title, String description, Long developerId, String attachmentUrl) {
        User developer = userRepository.findById(developerId)
                .orElseThrow(() -> new RuntimeException("Cannot create ticket: Developer ID " + developerId + " not found."));

        Ticket ticket = new Ticket();
        ticket.setTitle(title);
        ticket.setDescription(description);
        ticket.setDeveloper(developer);
        ticket.setStatus("PENDING");
        ticket.setAttachmentUrl(attachmentUrl);

        Ticket savedTicket = ticketRepository.save(ticket);
        
        logAction("TICKET", savedTicket.getId(), "CREATE", "Ticket created", developer);
        
        return savedTicket;
    }

    // Fetch all tickets (filtering out deleted ones)
    public List<Ticket> getAllTickets() {
        return ticketRepository.findAll().stream()
                .filter(t -> !t.isDeleted())
                .toList();
    }

    // Update Ticket Status
    @Transactional
    public Ticket updateTicketStatus(Long ticketId, String newStatus, User performedBy) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket ID " + ticketId + " not found."));
        
        String oldStatus = ticket.getStatus();
        ticket.setStatus(newStatus.toUpperCase());
        Ticket updatedTicket = ticketRepository.save(ticket);

        logAction("TICKET", ticketId, "STATUS_CHANGE", 
                "Changed status from " + oldStatus + " to " + newStatus.toUpperCase(), performedBy);
        
        return updatedTicket;
    }

    // Assign a Ticket
    @Transactional
    public Ticket assignTicket(Long ticketId, Long developerId, User performedBy) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket ID " + ticketId + " not found."));
        
        User developer = userRepository.findById(developerId)
                .orElseThrow(() -> new RuntimeException("Developer ID " + developerId + " not found."));

        ticket.setDeveloper(developer);
        Ticket updatedTicket = ticketRepository.save(ticket);

        logAction("TICKET", ticketId, "ASSIGNMENT", "Assigned to " + developer.getUsername(), performedBy);
        
        return updatedTicket;
    }

    @Transactional
    public void softDeleteTicket(Long ticketId, User performedBy) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket ID " + ticketId + " not found."));
        
        ticket.setDeleted(true);
        ticketRepository.save(ticket);

        logAction("TICKET", ticketId, "DELETE", "Ticket soft-deleted", performedBy);
    }

    public List<AuditLog> getTicketHistory(Long ticketId) {
        return auditLogRepository.findByEntityNameAndEntityIdOrderByTimestampDesc("TICKET", ticketId);
    }

    private void logAction(String entity, Long entityId, String action, String details, User performedBy) {
        AuditLog log = new AuditLog();
        log.setEntityName(entity);
        log.setEntityId(entityId);
        log.setAction(action);
        log.setDetails(details);
        log.setPerformedBy(performedBy);
        auditLogRepository.save(log);
    }
}