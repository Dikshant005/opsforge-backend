package com.opsforge.backend.dtos.dashboard;

import java.time.LocalDateTime;

public class AuditLogDTO {
    private Long id;
    private String entityName;
    private Long entityId;
    private String action;
    private String details;
    private String performedByUsername;
    private LocalDateTime timestamp;

    public AuditLogDTO() {}

    public AuditLogDTO(Long id, String entityName, Long entityId, String action, String details, String performedByUsername, LocalDateTime timestamp) {
        this.id = id;
        this.entityName = entityName;
        this.entityId = entityId;
        this.action = action;
        this.details = details;
        this.performedByUsername = performedByUsername;
        this.timestamp = timestamp;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEntityName() { return entityName; }
    public void setEntityName(String entityName) { this.entityName = entityName; }

    public Long getEntityId() { return entityId; }
    public void setEntityId(Long entityId) { this.entityId = entityId; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    public String getPerformedByUsername() { return performedByUsername; }
    public void setPerformedByUsername(String performedByUsername) { this.performedByUsername = performedByUsername; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
