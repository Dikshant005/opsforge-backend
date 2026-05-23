package com.opsforge.backend.controllers;

import com.opsforge.backend.models.AuditLog;
import com.opsforge.backend.models.Ticket;
import com.opsforge.backend.models.User;
import com.opsforge.backend.services.CloudinaryService;
import com.opsforge.backend.services.TicketService;
import com.opsforge.backend.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tickets")
@CrossOrigin(origins = "*")
public class TicketController {

    private final TicketService ticketService;
    private final UserService userService;
    private final CloudinaryService cloudinaryService;

    public TicketController(TicketService ticketService, UserService userService, CloudinaryService cloudinaryService) {
        this.ticketService = ticketService;
        this.userService = userService;
        this.cloudinaryService = cloudinaryService;
    }

    @GetMapping
    @Operation(summary = "Get all tickets", description = "Returns a list of all active tickets")
    public List<Ticket> getAllTickets() {
        return ticketService.getAllTickets();
    }

    @PostMapping(consumes = {"multipart/form-data"})
    @Operation(summary = "Create ticket", description = "Creates a new ticket with an optional attachment")
    public ResponseEntity<Ticket> createTicket(
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam(value = "file", required = false) MultipartFile file,
            Principal principal) {
        
        User user = userService.getUserByUsername(principal.getName());
        Long developerId = user.getId();

        String attachmentUrl = null;
        if (file != null && !file.isEmpty()) {
            attachmentUrl = cloudinaryService.uploadFile(file);
        }

        Ticket newTicket = ticketService.createTicket(title, description, developerId, attachmentUrl);
        return ResponseEntity.ok(newTicket);
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Update ticket status", description = "Changes the status of a specific ticket")
    public ResponseEntity<Ticket> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> payload, Principal principal) {
        String newStatus = payload.get("status");
        User user = userService.getUserByUsername(principal.getName());
        Ticket updatedTicket = ticketService.updateTicketStatus(id, newStatus, user);
        return ResponseEntity.ok(updatedTicket);
    }

    @PutMapping("/{id}/assign")
    @Operation(summary = "Assign ticket", description = "Reassigns a ticket to a different developer")
    public ResponseEntity<Ticket> assignTicket(@PathVariable Long id, @RequestBody Map<String, String> payload, Principal principal) {
        Long developerId = Long.parseLong(payload.get("developerId"));
        User user = userService.getUserByUsername(principal.getName());
        Ticket updatedTicket = ticketService.assignTicket(id, developerId, user);
        return ResponseEntity.ok(updatedTicket);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Soft delete ticket", description = "Marks a ticket as deleted without removing it from the database")
    public ResponseEntity<Map<String, String>> deleteTicket(@PathVariable Long id, Principal principal) {
        User user = userService.getUserByUsername(principal.getName());
        ticketService.softDeleteTicket(id, user);
        return ResponseEntity.ok(Map.of("message", "Ticket soft-deleted successfully"));
    }

    @GetMapping("/{id}/history")
    @Operation(summary = "Get ticket history", description = "Returns the audit log for a specific ticket")
    public List<AuditLog> getTicketHistory(@PathVariable Long id) {
        return ticketService.getTicketHistory(id);
    }
}