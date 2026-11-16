package com.messmate.model;

public final class PaymentStatus {
    public static final String PAID = "Paid";
    public static final String PENDING = "Pending";

    private PaymentStatus() {
    }

    public static boolean isAllowed(String status) {
        return PAID.equals(status) || PENDING.equals(status);
    }
}
