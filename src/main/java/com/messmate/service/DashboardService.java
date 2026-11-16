package com.messmate.service;

import com.messmate.db.MemberDao;
import com.messmate.db.MembershipDao;
import com.messmate.db.PaymentDao;
import com.messmate.model.DashboardSummary;
import com.messmate.util.AppException;

import java.sql.SQLException;
import java.time.LocalDate;

public class DashboardService {
    private final MemberDao memberDao;
    private final MembershipDao membershipDao;
    private final PaymentDao paymentDao;

    public DashboardService() {
        this(new MemberDao(), new MembershipDao(), new PaymentDao());
    }

    public DashboardService(MemberDao memberDao, MembershipDao membershipDao, PaymentDao paymentDao) {
        this.memberDao = memberDao;
        this.membershipDao = membershipDao;
        this.paymentDao = paymentDao;
    }

    public DashboardSummary load() {
        try {
            LocalDate today = LocalDate.now();
            return new DashboardSummary(
                    memberDao.count(),
                    membershipDao.countActive(today),
                    membershipDao.countExpired(today),
                    paymentDao.totalCollected(),
                    paymentDao.recent(8)
            );
        } catch (SQLException ex) {
            ex.printStackTrace();
            throw new AppException("Could not load the dashboard. Please try again.");
        }
    }
}
