package com.opsforge.backend.dtos.dashboard;

import java.util.List;

public class QADashboardDTO {
    private long readyForTestingCount;
    private long activelyTestingCount;
    private List<TicketDTO> readyForTestingTickets;
    private List<TicketDTO> myActiveTests;

    public QADashboardDTO() {}

    public long getReadyForTestingCount() { return readyForTestingCount; }
    public void setReadyForTestingCount(long readyForTestingCount) { this.readyForTestingCount = readyForTestingCount; }

    public long getActivelyTestingCount() { return activelyTestingCount; }
    public void setActivelyTestingCount(long activelyTestingCount) { this.activelyTestingCount = activelyTestingCount; }

    public List<TicketDTO> getReadyForTestingTickets() { return readyForTestingTickets; }
    public void setReadyForTestingTickets(List<TicketDTO> readyForTestingTickets) { this.readyForTestingTickets = readyForTestingTickets; }

    public List<TicketDTO> getMyActiveTests() { return myActiveTests; }
    public void setMyActiveTests(List<TicketDTO> myActiveTests) { this.myActiveTests = myActiveTests; }
}
