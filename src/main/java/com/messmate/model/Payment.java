package com.messmate.model;

import java.time.LocalDate;

public class Payment {
    private int id;
    private String receiptNumber;
    private int memberId;
    private String memberName;
    private String memberPhone;
    private double amount;
    private LocalDate paymentDate;
    private String paymentMonth;
    private String status;

    public Payment(int id, String receiptNumber, int memberId, String memberName, String memberPhone,
                   double amount, LocalDate paymentDate, String paymentMonth, String status) {
        this.id = id;
        this.receiptNumber = receiptNumber;
        this.memberId = memberId;
        this.memberName = memberName;
        this.memberPhone = memberPhone;
        this.amount = amount;
        this.paymentDate = paymentDate;
        this.paymentMonth = paymentMonth;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public String getReceiptNumber() {
        return receiptNumber;
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

    public double getAmount() {
        return amount;
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    public String getPaymentMonth() {
        return paymentMonth;
    }

    public String getStatus() {
        return status;
    }
}
