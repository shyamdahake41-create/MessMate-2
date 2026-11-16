package com.messmate.model;

import java.util.List;

public record DashboardSummary(
        int totalMembers,
        int activeMemberships,
        int expiredMemberships,
        double totalCollected,
        List<Payment> recentPayments
) {
}
