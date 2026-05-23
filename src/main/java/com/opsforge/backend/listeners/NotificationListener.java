package com.opsforge.backend.listeners;

import com.opsforge.backend.events.CommentEvent;
import com.opsforge.backend.events.TicketEvent;
import com.opsforge.backend.models.Ticket;
import com.opsforge.backend.models.User;
import org.springframework.context.event.EventListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class NotificationListener {

    private final JavaMailSender mailSender;

    public NotificationListener(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async
    @EventListener
    public void handleTicketEvent(TicketEvent event) {
        Ticket ticket = event.getTicket();
        User assignee = ticket.getDeveloper();
        
        String subject = "";
        String body = "";

        if (event.getAction().equals("ASSIGNED")) {
            subject = "OpsForge: New Ticket Assigned";
            body = "Hi " + assignee.getUsername() + ",\n\nA new ticket has been assigned to you:\n" +
                   "Title: " + ticket.getTitle() + "\n" +
                   "By: " + event.getPerformedBy().getUsername();
        } else if (event.getAction().equals("STATUS_CHANGED")) {
            subject = "OpsForge: Ticket Status Updated";
            body = "Hi " + assignee.getUsername() + ",\n\nYour ticket [" + ticket.getTitle() + "] status has been changed to " + ticket.getStatus() + 
                   " by " + event.getPerformedBy().getUsername();
        }

        sendEmail(assignee.getUsername(), subject, body);
    }

    @Async
    @EventListener
    public void handleCommentEvent(CommentEvent event) {
        Ticket ticket = event.getComment().getTicket();
        User assignee = ticket.getDeveloper();
        User author = event.getComment().getAuthor();

        // Don't notify the developer if they are the one who commented
        if (!assignee.getId().equals(author.getId())) {
            String subject = "OpsForge: New Comment on Ticket " + ticket.getId();
            String body = "Hi " + assignee.getUsername() + ",\n\n" + author.getUsername() + 
                          " added a comment to your ticket [" + ticket.getTitle() + "]:\n\n" +
                          event.getComment().getMessage();

            sendEmail(assignee.getUsername(), subject, body);
        }
    }

    private void sendEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Failed to send notification email to " + to + ": " + e.getMessage());
        }
    }
}
