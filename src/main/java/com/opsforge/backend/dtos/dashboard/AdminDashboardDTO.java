package com.opsforge.backend.dtos.dashboard;

import java.util.List;

public class AdminDashboardDTO {
    private long totalActiveUsers;
    private long totalOpenTickets;
    private long totalClosedTickets;
    private long pendingUserApprovals;
    private List<AuditLogDTO> recentSystemActivity;

    public AdminDashboardDTO() {}

    public long getTotalActiveUsers() { return totalActiveUsers; }
    public void setTotalActiveUsers(long totalActiveUsers) { this.totalActiveUsers = totalActiveUsers; }

    public long getTotalOpenTickets() { return totalOpenTickets; }
    public void setTotalOpenTickets(long totalOpenTickets) { this.totalOpenTickets = totalOpenTickets; }

    public long getTotalClosedTickets() { return totalClosedTickets; }
    public void setTotalClosedTickets(long totalClosedTickets) { this.totalClosedTickets = totalClosedTickets; }

    public long getPendingUserApprovals() { return pendingUserApprovals; }
    public void setPendingUserApprovals(long pendingUserApprovals) { this.pendingUserApprovals = pendingUserApprovals; }

    public List<AuditLogDTO> getRecentSystemActivity() { return recentSystemActivity; }
    public void setRecentSystemActivity(List<AuditLogDTO> recentSystemActivity) { this.recentSystemActivity = recentSystemActivity; }
}
