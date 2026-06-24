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
        java.util.List<User> developers = ticket.getDevelopers();
        
        String subject = "";
        String bodyBase = "";

        if (event.getAction().equals("ASSIGNED")) {
            subject = "OpsForge: New Ticket Assigned";
            bodyBase = "A new ticket has been assigned to you:\n" +
                   "Title: " + ticket.getTitle() + "\n" +
                   "By: " + event.getPerformedBy().getUsername();
        } else if (event.getAction().equals("STATUS_CHANGED")) {
            subject = "OpsForge: Ticket Status Updated";
            bodyBase = "Your ticket [" + ticket.getTitle() + "] status has been changed to " + ticket.getStatus() + 
                   " by " + event.getPerformedBy().getUsername();
        }

        for (User dev : developers) {
            sendEmail(dev.getUsername(), subject, "Hi " + dev.getUsername() + ",\n\n" + bodyBase);
        }
    }

    @Async
    @EventListener
    public void handleCommentEvent(CommentEvent event) {
        Ticket ticket = event.getComment().getTicket();
        java.util.List<User> developers = ticket.getDevelopers();
        User author = event.getComment().getAuthor();

        for (User dev : developers) {
            // Don't notify the developer if they are the one who commented
            if (!dev.getId().equals(author.getId())) {
                String subject = "OpsForge: New Comment on Ticket " + ticket.getId();
                String body = "Hi " + dev.getUsername() + ",\n\n" + author.getUsername() + 
                              " added a comment to your ticket [" + ticket.getTitle() + "]:\n\n" +
                              event.getComment().getMessage();

                sendEmail(dev.getUsername(), subject, body);
            }
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
