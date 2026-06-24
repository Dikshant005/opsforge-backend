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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public Ticket createTicket(String title, String description, Long creatorId, String attachmentUrl) {
        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new RuntimeException("Cannot create ticket: Creator ID " + creatorId + " not found."));

        Ticket ticket = new Ticket();
        ticket.setTitle(title);
        ticket.setDescription(description);
        ticket.setStatus("PENDING");
        ticket.setAttachmentUrl(attachmentUrl);

        Ticket savedTicket = ticketRepository.save(ticket);
        
        logAction("TICKET", savedTicket.getId(), "CREATE", "Ticket created", creator);
        eventPublisher.publishEvent(new TicketEvent(savedTicket, creator, "CREATED"));
        
        return savedTicket;
    }

    // Fetch all active tickets
    public List<Ticket> getAllTickets() {
        return ticketRepository.findByIsDeletedFalse();
    }

    public Ticket getTicketById(Long id) {
        return ticketRepository.findById(id)
                .filter(t -> !t.isDeleted())
                .orElseThrow(() -> new RuntimeException("Ticket not found or deleted"));
    }

    // Search and Filter Tickets
    public List<Ticket> searchTickets(String status, Long developerId) {
        User developer = null;
        if (developerId != null) {
            developer = userRepository.findById(developerId)
                    .orElseThrow(() -> new RuntimeException("Developer not found"));
        }

        if (status != null && developer != null) {
            return ticketRepository.findByStatusAndDevelopersContainingAndIsDeletedFalse(status.toUpperCase(), developer);
        } else if (status != null) {
            return ticketRepository.findByStatusAndIsDeletedFalse(status.toUpperCase());
        } else if (developer != null) {
            return ticketRepository.findByDevelopersContainingAndIsDeletedFalse(developer);
        } else {
            return getAllTickets();
        }
    }

    public Map<String, Long> getTicketStats() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("PENDING", ticketRepository.countByStatusAndIsDeletedFalse("PENDING"));
        stats.put("IN_PROGRESS", ticketRepository.countByStatusAndIsDeletedFalse("IN_PROGRESS"));
        stats.put("FIXED", ticketRepository.countByStatusAndIsDeletedFalse("FIXED"));
        stats.put("CLOSED", ticketRepository.countByStatusAndIsDeletedFalse("CLOSED"));
        return stats;
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
        if (userRole.equals("DEV") && ticket.getDevelopers().stream().noneMatch(d -> d.getId().equals(performedBy.getId()))) {
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

    // Assign a Ticket to multiple developers and QAs (Admin Only)
    @Transactional
    public Ticket assignTicket(Long ticketId, List<Long> developerIds, List<Long> qaIds, User performedBy) {
        String userRole = performedBy.getRole();
        
        // Only Admin can assign tickets now
        if (!userRole.equals("ADMIN")) {
            throw new RuntimeException("Access Denied: Only Admin can assign tickets.");
        }

        if (developerIds == null || developerIds.isEmpty() || qaIds == null || qaIds.isEmpty()) {
            throw new RuntimeException("Validation Error: At least 1 Developer and 1 QA must be assigned.");
        }

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket ID " + ticketId + " not found."));
        
        List<User> developers = userRepository.findAllById(developerIds);
        if (developers.isEmpty()) throw new RuntimeException("No valid developers found.");
        
        List<User> qas = userRepository.findAllById(qaIds);
        if (qas.isEmpty()) throw new RuntimeException("No valid QAs found.");

        ticket.setDevelopers(developers);
        ticket.setReviewers(qas);
        
        Ticket updatedTicket = ticketRepository.save(ticket);

        logAction("TICKET", ticketId, "ASSIGNMENT", "Assigned to multiple Devs and QAs", performedBy);
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