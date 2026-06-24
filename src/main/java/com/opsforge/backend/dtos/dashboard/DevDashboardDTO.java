package com.opsforge.backend.dtos.dashboard;

import java.util.List;

public class DevDashboardDTO {
    private long activeTicketsCount;
    private long ticketsNeedingReworkCount;
    private List<TicketDTO> activeTickets;
    private List<TicketDTO> reworkTickets;

    public DevDashboardDTO() {}

    public long getActiveTicketsCount() { return activeTicketsCount; }
    public void setActiveTicketsCount(long activeTicketsCount) { this.activeTicketsCount = activeTicketsCount; }

    public long getTicketsNeedingReworkCount() { return ticketsNeedingReworkCount; }
    public void setTicketsNeedingReworkCount(long ticketsNeedingReworkCount) { this.ticketsNeedingReworkCount = ticketsNeedingReworkCount; }

    public List<TicketDTO> getActiveTickets() { return activeTickets; }
    public void setActiveTickets(List<TicketDTO> activeTickets) { this.activeTickets = activeTickets; }

    public List<TicketDTO> getReworkTickets() { return reworkTickets; }
    public void setReworkTickets(List<TicketDTO> reworkTickets) { this.reworkTickets = reworkTickets; }
}
