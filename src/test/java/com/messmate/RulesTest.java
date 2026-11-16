package com.messmate;

import com.messmate.model.MembershipStatus;
import com.messmate.util.AppException;
import com.messmate.util.DateRanges;
import com.messmate.util.Validators;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RulesTest {
    @Test
    void membershipStatusUsesInclusiveDates() {
        LocalDate start = LocalDate.of(2026, 11, 1);
        LocalDate expiry = LocalDate.of(2026, 11, 30);
        assertEquals(MembershipStatus.UPCOMING, DateRanges.status(start, expiry, LocalDate.of(2026, 10, 31)));
        assertEquals(MembershipStatus.ACTIVE, DateRanges.status(start, expiry, start));
        assertEquals(MembershipStatus.ACTIVE, DateRanges.status(start, expiry, expiry));
        assertEquals(MembershipStatus.EXPIRED, DateRanges.status(start, expiry, LocalDate.of(2026, 12, 1)));
    }

    @Test
    void oneMonthRunsThroughTheDayBeforeTheSameDateNextMonth() {
        assertEquals(LocalDate.of(2026, 12, 15), DateRanges.expiryFor(LocalDate.of(2026, 11, 16)));
        assertEquals(LocalDate.of(2026, 2, 27), DateRanges.expiryFor(LocalDate.of(2026, 1, 31)));
    }

    @Test
    void renewalContinuesAfterAnActivePeriodAndRestartsAfterExpiry() {
        LocalDate today = LocalDate.of(2026, 11, 16);
        assertEquals(LocalDate.of(2026, 12, 16), DateRanges.renewalStart(LocalDate.of(2026, 12, 15), today));
        assertEquals(today, DateRanges.renewalStart(LocalDate.of(2026, 10, 1), today));
    }

    @Test
    void overlappingPeriodsAreDetected() {
        LocalDate start = LocalDate.of(2026, 11, 1);
        LocalDate end = LocalDate.of(2026, 11, 30);
        assertTrue(DateRanges.overlaps(start, end, LocalDate.of(2026, 11, 30), LocalDate.of(2026, 12, 29)));
        assertFalse(DateRanges.overlaps(start, end, LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 31)));
    }

    @Test
    void memberFieldsAreValidated() {
        assertEquals("Asha Rao", Validators.requireName("  Asha   Rao "));
        assertEquals("9876543210", Validators.requirePhone("98765-43210"));
        assertEquals(2500.5, Validators.parseMoney("Monthly fee", "2500.50"));
        assertEquals("2026-11", Validators.requireMonth("2026-11"));
        assertThrows(AppException.class, () -> Validators.requirePhone("12345"));
        assertThrows(AppException.class, () -> Validators.parseMoney("Monthly fee", "0"));
        assertThrows(AppException.class, () -> Validators.parseMoney("Monthly fee", "12.345"));
        assertThrows(AppException.class, () -> Validators.requireName("Asha1"));
        assertThrows(AppException.class, () -> Validators.requireMonth("11-2026"));
    }
}
