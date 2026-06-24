package com.opsforge.backend.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tickets")
public class Ticket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private String status = "PENDING"; // Default value

    // Relationships

    // The Developers assigned to it
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "ticket_developers",
        joinColumns = @JoinColumn(name = "ticket_id"),
        inverseJoinColumns = @JoinColumn(name = "developer_id")
    )
    private java.util.List<User> developers = new java.util.ArrayList<>();

    // The QAs assigned to review it
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "ticket_qas",
        joinColumns = @JoinColumn(name = "ticket_id"),
        inverseJoinColumns = @JoinColumn(name = "reviewer_id")
    )
    private java.util.List<User> reviewers = new java.util.ArrayList<>();

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private boolean isDeleted = false;

    @Column
    private String attachmentUrl;

    // Automatically set the timestamp when created
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public java.util.List<User> getDevelopers() { return developers; }
    public void setDevelopers(java.util.List<User> developers) { this.developers = developers; }

    public java.util.List<User> getReviewers() { return reviewers; }
    public void setReviewers(java.util.List<User> reviewers) { this.reviewers = reviewers; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public boolean isDeleted() { return isDeleted; }
    public void setDeleted(boolean deleted) { isDeleted = deleted; }

    public String getAttachmentUrl() { return attachmentUrl; }
    public void setAttachmentUrl(String attachmentUrl) { this.attachmentUrl = attachmentUrl; }
}
