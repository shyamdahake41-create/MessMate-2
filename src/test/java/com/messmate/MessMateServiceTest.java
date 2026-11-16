package com.messmate;

import com.messmate.db.DatabaseManager;
import com.messmate.model.DashboardSummary;
import com.messmate.model.Member;
import com.messmate.model.Membership;
import com.messmate.model.Payment;
import com.messmate.model.PaymentStatus;
import com.messmate.service.AuthService;
import com.messmate.service.DashboardService;
import com.messmate.service.MemberService;
import com.messmate.service.MembershipService;
import com.messmate.service.PaymentService;
import com.messmate.service.ReceiptService;
import com.messmate.util.AppException;
import com.messmate.util.DateRanges;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.YearMonth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MessMateServiceTest {
    @TempDir
    Path temp;

    private AuthService auth;
    private MemberService members;
    private MembershipService memberships;
    private PaymentService payments;
    private DashboardService dashboard;

    @BeforeEach
    void setUp() throws Exception {
        DatabaseManager.initialize(temp.resolve("messmate.db"));
        auth = new AuthService();
        members = new MemberService();
        memberships = new MembershipService();
        payments = new PaymentService();
        dashboard = new DashboardService();
    }

    @Test
    void defaultAdminCanLogInAndWrongPasswordCannot() {
        assertTrue(auth.login("admin", "admin123"));
        assertFalse(auth.login("admin", "wrong"));
        assertFalse(auth.login("", "admin123"));
    }

    @Test
    void membersCanBeAddedSearchedUpdatedDeletedAndReloaded() throws Exception {
        Member created = members.add(sample("Asha Rao", "9876543210"));
        assertEquals(1, members.search("asha").size());
        assertEquals(1, members.search("98765").size());

        created.setAddress("Hostel Block B, Room 4");
        created.setMonthlyFee(2800);
        members.update(created);
        assertEquals(2800, members.get(created.getId()).orElseThrow().getMonthlyFee());

        Path database = temp.resolve("reopen.db");
        DatabaseManager.initialize(database);
        Member kept = members.add(sample("Meera Nair", "9123456780"));
        DatabaseManager.initialize(database);
        assertEquals("Meera Nair", members.get(kept.getId()).orElseThrow().getName());

        members.delete(kept.getId());
        assertTrue(members.get(kept.getId()).isEmpty());
    }

    @Test
    void membershipStatusAndRenewalFollowTheDates() {
        Member member = members.add(sample("Rahul Jain", "9988776655"));
        LocalDate today = LocalDate.now();

        Membership active = memberships.assign(member.getId(), today, 2500);
        assertEquals("Active", active.getStatus());
        assertEquals(DateRanges.expiryFor(today), active.getExpiryDate());

        assertThrows(AppException.class, () -> memberships.assign(member.getId(), today, 2500));

        Membership renewed = memberships.renew(member.getId());
        assertEquals(active.getExpiryDate().plusDays(1), renewed.getStartDate());
        assertEquals("Upcoming", renewed.getStatus());

        Member expiredMember = members.add(sample("Old Member", "9012345678"));
        Membership expired = memberships.assign(expiredMember.getId(), today.minusMonths(3), 1800);
        assertEquals("Expired", expired.getStatus());
        Membership restarted = memberships.renew(expiredMember.getId());
        assertEquals(today, restarted.getStartDate());
        assertEquals("Active", restarted.getStatus());

        DashboardSummary summary = dashboard.load();
        assertEquals(2, summary.totalMembers());
        assertEquals(2, summary.activeMemberships());
        assertEquals(1, summary.expiredMemberships());
    }

    @Test
    void duplicatePaymentsAreRejectedAndReceiptNumbersStayUnique() throws Exception {
        Member member = members.add(sample("Kavya Shah", "9090909090"));
        String month = YearMonth.now().toString();
        Payment first = payments.record(member.getId(), 2500, LocalDate.now(), month, PaymentStatus.PAID);
        Payment otherMonth = payments.record(member.getId(), 500, LocalDate.now(), YearMonth.now().minusMonths(1).toString(), PaymentStatus.PENDING);

        memberships.assign(member.getId(), LocalDate.now().minusMonths(2), 2500);
        assertEquals("RCP-1001", first.getReceiptNumber());
        assertEquals("RCP-1002", otherMonth.getReceiptNumber());
        AppException duplicate = assertThrows(AppException.class,
                () -> payments.record(member.getId(), 2500, LocalDate.now(), month, PaymentStatus.PAID));
        assertTrue(duplicate.getMessage().contains("already exists"));

        DashboardSummary summary = dashboard.load();
        assertEquals(2500, summary.totalCollected());
        assertEquals(2, summary.recentPayments().size());

        members.delete(member.getId());
        assertTrue(payments.search("").isEmpty());
        assertTrue(memberships.list("All").isEmpty());

        Path pdf = temp.resolve("receipt.pdf");
        ReceiptService.writePdf(first, pdf);
        byte[] bytes = Files.readAllBytes(pdf);
        assertTrue(bytes.length > 100);
        assertEquals("%PDF", new String(bytes, 0, 4));
    }

    private static Member sample(String name, String phone) {
        Member member = new Member();
        member.setName(name);
        member.setPhone(phone);
        member.setAddress("Hostel Block A, Room 12");
        member.setJoiningDate(LocalDate.now());
        member.setMonthlyFee(2500);
        return member;
    }
}
