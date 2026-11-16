package com.messmate.util;

import java.math.BigDecimal;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.YearMonth;

public final class Validators {
    private Validators() {
    }

    public static String requireName(String name) {
        if (name == null || name.isBlank()) {
            throw new AppException("Name is required.");
        }
        String trimmed = name.trim().replaceAll("\\s+", " ");
        if (trimmed.length() < 2 || trimmed.length() > 80) {
            throw new AppException("Name must be between 2 and 80 characters.");
        }
        if (!trimmed.matches("[\\p{L}][\\p{L} .'-]*")) {
            throw new AppException("Name can contain only letters, spaces, apostrophes, dots and hyphens.");
        }
        return trimmed;
    }

    public static String requirePhone(String phone) {
        if (phone == null || phone.isBlank()) {
            throw new AppException("Phone number is required.");
        }
        String digits = phone.trim().replaceAll("[\\s-]", "");
        if (!digits.matches("\\d{10}")) {
            throw new AppException("Phone number must be exactly 10 digits.");
        }
        return digits;
    }

    public static String requireAddress(String address) {
        if (address == null || address.isBlank()) {
            throw new AppException("Address is required.");
        }
        String trimmed = address.trim().replaceAll("[\\r\\n]+", ", ");
        if (trimmed.length() < 3 || trimmed.length() > 200) {
            throw new AppException("Address must be between 3 and 200 characters.");
        }
        return trimmed;
    }

    public static void requireDate(LocalDate date, String label) {
        if (date == null) {
            throw new AppException(label + " is required.");
        }
        if (date.isBefore(LocalDate.of(2000, 1, 1)) || date.isAfter(LocalDate.of(2100, 12, 31))) {
            throw new AppException(label + " must be between 2000 and 2100.");
        }
    }

    public static double parseMoney(String label, String text) {
        if (text == null || text.isBlank()) {
            throw new AppException(label + " is required.");
        }
        BigDecimal value;
        try {
            value = new BigDecimal(text.trim());
        } catch (NumberFormatException ex) {
            throw new AppException(label + " must be a valid number.");
        }
        value = value.stripTrailingZeros();
        if (value.scale() < 0) {
            value = value.setScale(0);
        }
        if (value.scale() > 2) {
            throw new AppException(label + " can have at most 2 decimal places.");
        }
        return requireMoney(label, value.doubleValue());
    }

    public static double requireMoney(String label, double amount) {
        if (Double.isNaN(amount) || Double.isInfinite(amount) || amount <= 0) {
            throw new AppException(label + " must be greater than zero.");
        }
        if (amount > 100000) {
            throw new AppException(label + " cannot exceed 100000.");
        }
        return Math.round(amount * 100.0) / 100.0;
    }

    public static String requireMonth(String month) {
        if (month == null || month.isBlank()) {
            throw new AppException("Payment month is required.");
        }
        try {
            return YearMonth.parse(month.trim()).toString();
        } catch (DateTimeException ex) {
            throw new AppException("Payment month must use yyyy-MM.");
        }
    }
}
