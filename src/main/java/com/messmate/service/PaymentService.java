package com.messmate.service;

import com.messmate.db.DatabaseManager;
import com.messmate.db.MemberDao;
import com.messmate.db.PaymentDao;
import com.messmate.model.Payment;
import com.messmate.model.PaymentStatus;
import com.messmate.util.AppException;
import com.messmate.util.Validators;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

public class PaymentService {
    private final MemberDao memberDao;
    private final PaymentDao paymentDao;

    public PaymentService() {
        this(new MemberDao(), new PaymentDao());
    }

    public PaymentService(MemberDao memberDao, PaymentDao paymentDao) {
        this.memberDao = memberDao;
        this.paymentDao = paymentDao;
    }

    public Payment record(int memberId, double amount, LocalDate paymentDate, String paymentMonth, String status) {
        double paidAmount = Validators.requireMoney("Payment amount", amount);
        Validators.requireDate(paymentDate, "Payment date");
        String month = Validators.requireMonth(paymentMonth);
        if (!PaymentStatus.isAllowed(status)) {
            throw new AppException("Payment status must be Paid or Pending.");
        }
        int paymentId;
        try (Connection connection = DatabaseManager.getConnection()) {
            connection.setAutoCommit(false);
            try {
                if (!memberDao.exists(connection, memberId)) {
                    throw new AppException("Selected member was not found.");
                }
                if (paymentDao.exists(connection, memberId, month)) {
                    throw new AppException("A payment for this member and month already exists.");
                }
                String receiptNumber = paymentDao.nextReceiptNumber(connection);
                paymentId = paymentDao.insert(connection, receiptNumber, memberId, paidAmount, paymentDate, month, status);
                connection.commit();
            } catch (Exception ex) {
                rollback(connection);
                if (ex instanceof AppException appException) {
                    throw appException;
                }
                if (ex instanceof SQLException sqlException) {
                    throw duplicateOrFailure(sqlException);
                }
                if (ex instanceof RuntimeException runtimeException) {
                    throw runtimeException;
                }
                throw new AppException("Could not record the payment. Please try again.");
            }
        } catch (SQLException ex) {
            throw duplicateOrFailure(ex);
        }
        try {
            return paymentDao.find(paymentId).orElseThrow(() -> new AppException("The payment was saved but could not be reloaded."));
        } catch (SQLException ex) {
            throw failure(ex);
        }
    }

    public List<Payment> search(String query) {
        try {
            return paymentDao.search(query);
        } catch (SQLException ex) {
            throw failure(ex);
        }
    }

    private static void rollback(Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private static AppException duplicateOrFailure(SQLException ex) {
        String message = ex.getMessage() == null ? "" : ex.getMessage().toLowerCase(Locale.ROOT);
        if (message.contains("unique") || message.contains("constraint")) {
            return new AppException("A payment for this member and month already exists.");
        }
        ex.printStackTrace();
        return new AppException("Could not record the payment. Please try again.");
    }

    private static AppException failure(SQLException ex) {
        ex.printStackTrace();
        return new AppException("Could not load payments. Please try again.");
    }
}
