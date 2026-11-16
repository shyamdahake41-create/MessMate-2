package com.messmate.model;

import java.time.LocalDate;

public class Member {
    private Integer id;
    private String name;
    private String phone;
    private String address;
    private LocalDate joiningDate;
    private double monthlyFee;

    public Member() {
    }

    public Member(Integer id, String name, String phone, String address, LocalDate joiningDate, double monthlyFee) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.address = address;
        this.joiningDate = joiningDate;
        this.monthlyFee = monthlyFee;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public LocalDate getJoiningDate() {
        return joiningDate;
    }

    public void setJoiningDate(LocalDate joiningDate) {
        this.joiningDate = joiningDate;
    }

    public double getMonthlyFee() {
        return monthlyFee;
    }

    public void setMonthlyFee(double monthlyFee) {
        this.monthlyFee = monthlyFee;
    }
}
