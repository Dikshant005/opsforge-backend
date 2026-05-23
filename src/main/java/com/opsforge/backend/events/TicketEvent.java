package com.opsforge.backend.events;

import com.opsforge.backend.models.Ticket;
import com.opsforge.backend.models.User;

public class TicketEvent {
    private final Ticket ticket;
    private final User performedBy;
    private final String action; // ASSIGNED, STATUS_CHANGED

    public TicketEvent(Ticket ticket, User performedBy, String action) {
        this.ticket = ticket;
        this.performedBy = performedBy;
        this.action = action;
    }

    public Ticket getTicket() { return ticket; }
    public User getPerformedBy() { return performedBy; }
    public String getAction() { return action; }
}
