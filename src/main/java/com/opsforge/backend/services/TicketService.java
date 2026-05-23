package com.opsforge.backend.services;

import com.opsforge.backend.events.TicketEvent;
import com.opsforge.backend.models.AuditLog;
import com.opsforge.backend.models.Ticket;
import com.opsforge.backend.models.User;
import com.opsforge.backend.repositories.AuditLogRepository;
import com.opsforge.backend.repositories.TicketRepository;
import com.opsforge.backend.repositories.UserRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TicketService {

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;
    private final ApplicationEventPublisher eventPublisher;

    public TicketService(TicketRepository ticketRepository, 
                         UserRepository userRepository, 
                         AuditLogRepository auditLogRepository,
                         ApplicationEventPublisher eventPublisher) {
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.auditLogRepository = auditLogRepository;
        this.eventPublisher = eventPublisher;
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
        eventPublisher.publishEvent(new TicketEvent(savedTicket, developer, "ASSIGNED"));
        
        return savedTicket;
    }

    // Fetch all active tickets
    public List<Ticket> getAllTickets() {
        return ticketRepository.findByIsDeletedFalse();
    }

    // Search and Filter Tickets
    public List<Ticket> searchTickets(String status, Long developerId) {
        User developer = null;
        if (developerId != null) {
            developer = userRepository.findById(developerId)
                    .orElseThrow(() -> new RuntimeException("Developer not found"));
        }

        if (status != null && developer != null) {
            return ticketRepository.findByStatusAndDeveloperAndIsDeletedFalse(status.toUpperCase(), developer);
        } else if (status != null) {
            return ticketRepository.findByStatusAndIsDeletedFalse(status.toUpperCase());
        } else if (developer != null) {
            return ticketRepository.findByDeveloperAndIsDeletedFalse(developer);
        } else {
            return getAllTickets();
        }
    }

    // Update Ticket Status with State Machine and Role-based permissions
    @Transactional
    public Ticket updateTicketStatus(Long ticketId, String newStatus, User performedBy) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket ID " + ticketId + " not found."));
        
        String oldStatus = ticket.getStatus();
        String targetStatus = newStatus.toUpperCase();
        String userRole = performedBy.getRole();

        // 1. Authorization Check: Dev can only update their own tickets
        if (userRole.equals("DEV") && !ticket.getDeveloper().getId().equals(performedBy.getId())) {
            throw new RuntimeException("Access Denied: You can only update tickets assigned to you.");
        }

        // 2. State Machine Logic
        if (!userRole.equals("ADMIN")) { // Admin has god mode
            if (userRole.equals("DEV")) {
                if (!(oldStatus.equals("PENDING") && targetStatus.equals("IN_PROGRESS")) &&
                    !(oldStatus.equals("IN_PROGRESS") && targetStatus.equals("FIXED"))) {
                    throw new RuntimeException("Invalid Transition: Developers can only move PENDING -> IN_PROGRESS or IN_PROGRESS -> FIXED.");
                }
            } else if (userRole.equals("QA")) {
                if (!(oldStatus.equals("FIXED") && targetStatus.equals("CLOSED")) &&
                    !(oldStatus.equals("FIXED") && targetStatus.equals("IN_PROGRESS"))) {
                    throw new RuntimeException("Invalid Transition: QA can only move FIXED -> CLOSED or FIXED -> IN_PROGRESS.");
                }
            }
        }

        ticket.setStatus(targetStatus);
        Ticket updatedTicket = ticketRepository.save(ticket);

        logAction("TICKET", ticketId, "STATUS_CHANGE", 
                "Changed status from " + oldStatus + " to " + targetStatus, performedBy);
        
        eventPublisher.publishEvent(new TicketEvent(updatedTicket, performedBy, "STATUS_CHANGED"));
        
        return updatedTicket;
    }

    // Assign a Ticket with Role-based permissions
    @Transactional
    public Ticket assignTicket(Long ticketId, Long developerId, User performedBy) {
        String userRole = performedBy.getRole();
        
        // Only QA and Admin can assign tickets
        if (!userRole.equals("ADMIN") && !userRole.equals("QA")) {
            throw new RuntimeException("Access Denied: Only QA or Admin can assign tickets.");
        }

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket ID " + ticketId + " not found."));
        
        User developer = userRepository.findById(developerId)
                .orElseThrow(() -> new RuntimeException("Developer ID " + developerId + " not found."));

        ticket.setDeveloper(developer);
        Ticket updatedTicket = ticketRepository.save(ticket);

        logAction("TICKET", ticketId, "ASSIGNMENT", "Assigned to " + developer.getUsername(), performedBy);
        eventPublisher.publishEvent(new TicketEvent(updatedTicket, performedBy, "ASSIGNED"));
        
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