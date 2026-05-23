package com.opsforge.backend.repositories;

import com.opsforge.backend.models.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    
    // Custom method to fetch all comments associated with a specific ticket ID
    List<Comment> findByTicketId(Long ticketId);
}