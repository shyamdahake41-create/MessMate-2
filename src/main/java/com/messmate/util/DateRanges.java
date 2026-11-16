package com.messmate.util;

import com.messmate.model.MembershipStatus;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

public final class DateRanges {
    private DateRanges() {
    }

    public static LocalDate expiryFor(LocalDate start) {
        return start.plusMonths(1).minusDays(1);
    }

    public static LocalDate renewalStart(LocalDate latestExpiry, LocalDate today) {
        if (latestExpiry.isBefore(today)) {
            return today;
        }
        return latestExpiry.plusDays(1);
    }

    public static boolean overlaps(LocalDate start, LocalDate end, LocalDate otherStart, LocalDate otherEnd) {
        return !start.isAfter(otherEnd) && !end.isBefore(otherStart);
    }

    public static MembershipStatus status(LocalDate start, LocalDate expiry, LocalDate today) {
        if (today.isBefore(start)) {
            return MembershipStatus.UPCOMING;
        }
        if (today.isAfter(expiry)) {
            return MembershipStatus.EXPIRED;
        }
        return MembershipStatus.ACTIVE;
    }

    public static LocalDate toLocalDate(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    public static Date toDate(LocalDate date) {
        return Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }
}
