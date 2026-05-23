package com.opsforge.backend.services;

import com.opsforge.backend.models.Comment;
import com.opsforge.backend.models.Ticket;
import com.opsforge.backend.models.User;
import com.opsforge.backend.repositories.CommentRepository;
import com.opsforge.backend.repositories.TicketRepository;
import com.opsforge.backend.repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;

    // Orchestrating three different database tables to create a comment
    public CommentService(CommentRepository commentRepository, TicketRepository ticketRepository, UserRepository userRepository) {
        this.commentRepository = commentRepository;
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
    }

    //  Add a new comment to a ticket
    public Comment addComment(Long ticketId, Long authorId, String message, String attachmentUrl) {
        
        // Check if the ticket is real
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Cannot add comment: Ticket ID " + ticketId + " not found."));

        // Check if the user is real
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new RuntimeException("Cannot add comment: User ID " + authorId + " not found."));

        // Assemble the Comment
        Comment comment = new Comment();
        comment.setMessage(message);
        comment.setTicket(ticket);
        comment.setAuthor(author);
        comment.setAttachmentUrl(attachmentUrl);

        // Save to PostgreSQL
        return commentRepository.save(comment);
    }

    // Fetch all comments for a specific ticket
    public List<Comment> getCommentsForTicket(Long ticketId) {
        //  utilizes the custom method defined in the CommentRepository interface
        return commentRepository.findByTicketId(ticketId);
    }
}