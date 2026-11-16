package com.messmate.service;

import com.messmate.db.MemberDao;
import com.messmate.db.MembershipDao;
import com.messmate.model.Member;
import com.messmate.model.Membership;
import com.messmate.util.AppException;
import com.messmate.util.DateRanges;
import com.messmate.util.Validators;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class MembershipService {
    private final MemberDao memberDao;
    private final MembershipDao membershipDao;

    public MembershipService() {
        this(new MemberDao(), new MembershipDao());
    }

    public MembershipService(MemberDao memberDao, MembershipDao membershipDao) {
        this.memberDao = memberDao;
        this.membershipDao = membershipDao;
    }

    public Membership assign(int memberId, LocalDate start, double fee) {
        Validators.requireDate(start, "Start date");
        double monthlyFee = Validators.requireMoney("Monthly fee", fee);
        LocalDate expiry = DateRanges.expiryFor(start);
        try {
            memberDao.find(memberId).orElseThrow(() -> new AppException("Selected member was not found."));
            for (Membership existing : membershipDao.listByMember(memberId)) {
                if (DateRanges.overlaps(start, expiry, existing.getStartDate(), existing.getExpiryDate())) {
                    throw new AppException("This member already has a membership covering those dates. Renew it for another month instead.");
                }
            }
            int id = membershipDao.insert(memberId, start, expiry, monthlyFee);
            return membershipDao.find(id).orElseThrow(() -> new AppException("The membership was saved but could not be reloaded."));
        } catch (SQLException ex) {
            throw failure("assign the membership", ex);
        }
    }

    public Membership renew(int memberId) {
        try {
            Member member = memberDao.find(memberId).orElseThrow(() -> new AppException("Selected member was not found."));
            Membership latest = membershipDao.latest(memberId)
                    .orElseThrow(() -> new AppException("Assign a membership before renewing."));
            LocalDate today = LocalDate.now();
            LocalDate start = DateRanges.renewalStart(latest.getExpiryDate(), today);
            LocalDate expiry = DateRanges.expiryFor(start);
            for (Membership existing : membershipDao.listByMember(memberId)) {
                if (DateRanges.overlaps(start, expiry, existing.getStartDate(), existing.getExpiryDate())) {
                    throw new AppException("The next month is already covered by an existing membership.");
                }
            }
            int id = membershipDao.insert(memberId, start, expiry, member.getMonthlyFee());
            return membershipDao.find(id).orElseThrow(() -> new AppException("The renewal was saved but could not be reloaded."));
        } catch (SQLException ex) {
            throw failure("renew the membership", ex);
        }
    }

    public List<Membership> list(String status) {
        try {
            List<Membership> rows = membershipDao.listAll();
            if (status == null || status.isBlank() || "All".equals(status)) {
                return rows;
            }
            return rows.stream().filter(row -> status.equals(row.getStatus())).toList();
        } catch (SQLException ex) {
            throw failure("load memberships", ex);
        }
    }

    private static AppException failure(String action, SQLException ex) {
        ex.printStackTrace();
        return new AppException("Could not " + action + ". Please try again.");
    }
}
