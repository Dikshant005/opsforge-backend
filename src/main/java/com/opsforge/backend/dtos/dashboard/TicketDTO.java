package com.opsforge.backend.dtos.dashboard;

import java.time.LocalDateTime;

public class TicketDTO {
    private Long id;
    private String title;
    private String status;
    private java.util.List<String> developerNames;
    private java.util.List<String> reviewerNames;
    private LocalDateTime createdAt;

    public TicketDTO() {}

    public TicketDTO(Long id, String title, String status, java.util.List<String> developerNames, java.util.List<String> reviewerNames, LocalDateTime createdAt) {
        this.id = id;
        this.title = title;
        this.status = status;
        this.developerNames = developerNames;
        this.reviewerNames = reviewerNames;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public java.util.List<String> getDeveloperNames() { return developerNames; }
    public void setDeveloperNames(java.util.List<String> developerNames) { this.developerNames = developerNames; }
    public java.util.List<String> getReviewerNames() { return reviewerNames; }
    public void setReviewerNames(java.util.List<String> reviewerNames) { this.reviewerNames = reviewerNames; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
