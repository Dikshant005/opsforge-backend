package com.opsforge.backend.services;

import com.opsforge.backend.dtos.dashboard.*;
import com.opsforge.backend.models.AuditLog;
import com.opsforge.backend.models.Ticket;
import com.opsforge.backend.models.User;
import com.opsforge.backend.repositories.AuditLogRepository;
import com.opsforge.backend.repositories.TicketRepository;
import com.opsforge.backend.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    public DevDashboardDTO getDevDashboard(User developer) {
        DevDashboardDTO dto = new DevDashboardDTO();
        
        List<String> activeStatuses = Arrays.asList("PENDING", "IN_PROGRESS");
        long activeCount = ticketRepository.countByDevelopersContainingAndStatusInAndIsDeletedFalse(developer, activeStatuses);
        dto.setActiveTicketsCount(activeCount);
        
        List<Ticket> activeTickets = ticketRepository.findTop5ByDevelopersContainingAndStatusInAndIsDeletedFalseOrderByCreatedAtDesc(developer, activeStatuses);
        dto.setActiveTickets(activeTickets.stream().map(this::mapToTicketDTO).collect(Collectors.toList()));
        
        return dto;
    }

    public QADashboardDTO getQADashboard(User qaUser) {
        QADashboardDTO dto = new QADashboardDTO();
        
        long readyCount = ticketRepository.countByStatusAndIsDeletedFalse("FIXED");
        dto.setReadyForTestingCount(readyCount);
        
        List<Ticket> readyTickets = ticketRepository.findTop5ByStatusAndIsDeletedFalseOrderByCreatedAtDesc("FIXED");
        dto.setReadyForTestingTickets(readyTickets.stream().map(this::mapToTicketDTO).collect(Collectors.toList()));
        
        return dto;
    }

    public AdminDashboardDTO getAdminDashboard() {
        AdminDashboardDTO dto = new AdminDashboardDTO();
        
        long activeUsers = userRepository.countByIsActiveTrue();
        dto.setTotalActiveUsers(activeUsers);
        
        long openTickets = ticketRepository.countByStatusNotAndIsDeletedFalse("CLOSED");
        dto.setTotalOpenTickets(openTickets);
        
        long closedTickets = ticketRepository.countByStatusAndIsDeletedFalse("CLOSED");
        dto.setTotalClosedTickets(closedTickets);
        
        long pendingApprovals = userRepository.countByAccountStatus("PENDING");
        dto.setPendingUserApprovals(pendingApprovals);
        
        List<AuditLog> recentLogs = auditLogRepository.findTop10ByOrderByTimestampDesc();
        dto.setRecentSystemActivity(recentLogs.stream().map(this::mapToAuditLogDTO).collect(Collectors.toList()));
        
        return dto;
    }

    private TicketDTO mapToTicketDTO(Ticket ticket) {
        TicketDTO dto = new TicketDTO();
        dto.setId(ticket.getId());
        dto.setTitle(ticket.getTitle());
        dto.setStatus(ticket.getStatus());
        dto.setDeveloperNames(ticket.getDevelopers().stream().map(User::getUsername).collect(Collectors.toList()));
        dto.setReviewerNames(ticket.getReviewers().stream().map(User::getUsername).collect(Collectors.toList()));
        dto.setCreatedAt(ticket.getCreatedAt());
        return dto;
    }

    private AuditLogDTO mapToAuditLogDTO(AuditLog log) {
        AuditLogDTO dto = new AuditLogDTO();
        dto.setId(log.getId());
        dto.setEntityName(log.getEntityName());
        dto.setEntityId(log.getEntityId());
        dto.setAction(log.getAction());
        dto.setDetails(log.getDetails());
        dto.setPerformedByUsername(log.getPerformedBy() != null ? log.getPerformedBy().getUsername() : "System");
        dto.setTimestamp(log.getTimestamp());
        return dto;
    }
}
