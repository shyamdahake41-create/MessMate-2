package com.messmate.model;

public enum MembershipStatus {
    ACTIVE("Active"),
    EXPIRED("Expired"),
    UPCOMING("Upcoming");

    private final String label;

    MembershipStatus(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}
