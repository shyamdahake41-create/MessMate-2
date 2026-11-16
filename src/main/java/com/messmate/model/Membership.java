package com.messmate.model;

import java.time.LocalDate;

public class Membership {
    private int id;
    private int memberId;
    private String memberName;
    private String memberPhone;
    private LocalDate startDate;
    private LocalDate expiryDate;
    private double monthlyFee;
    private String status;

    public Membership(int id, int memberId, String memberName, String memberPhone,
                      LocalDate startDate, LocalDate expiryDate, double monthlyFee, String status) {
        this.id = id;
        this.memberId = memberId;
        this.memberName = memberName;
        this.memberPhone = memberPhone;
        this.startDate = startDate;
        this.expiryDate = expiryDate;
        this.monthlyFee = monthlyFee;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public int getMemberId() {
        return memberId;
    }

    public String getMemberName() {
        return memberName;
    }

    public String getMemberPhone() {
        return memberPhone;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public double getMonthlyFee() {
        return monthlyFee;
    }

    public String getStatus() {
        return status;
    }
}
