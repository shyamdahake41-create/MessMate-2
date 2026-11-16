package com.messmate.db;

import com.messmate.model.Payment;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PaymentDao {
    private static final String SELECT = """
            SELECT p.id, p.receipt_number, p.member_id, m.name AS member_name, m.phone AS member_phone,
                   p.amount, p.payment_date, p.payment_month, p.status
            FROM payments p
            JOIN members m ON m.id = p.member_id
            """;

    public boolean exists(Connection connection, int memberId, String paymentMonth) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT 1 FROM payments WHERE member_id = ? AND payment_month = ?")) {
            statement.setInt(1, memberId);
            statement.setString(2, paymentMonth);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next();
            }
        }
    }

    public String nextReceiptNumber(Connection connection) throws SQLException {
        try (PreparedStatement update = connection.prepareStatement(
                "UPDATE receipt_counter SET last_value = last_value + 1 WHERE id = 1")) {
            if (update.executeUpdate() != 1) {
                throw new SQLException("Receipt counter is missing.");
            }
        }
        try (PreparedStatement query = connection.prepareStatement(
                "SELECT last_value FROM receipt_counter WHERE id = 1");
             ResultSet rs = query.executeQuery()) {
            if (!rs.next()) {
                throw new SQLException("Receipt counter is missing.");
            }
            return String.format("RCP-%04d", rs.getInt(1));
        }
    }

    public int insert(Connection connection, String receiptNumber, int memberId, double amount,
                      LocalDate paymentDate, String paymentMonth, String status) throws SQLException {
        String sql = """
                INSERT INTO payments(receipt_number, member_id, amount, payment_date, payment_month, status)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, receiptNumber);
            statement.setInt(2, memberId);
            statement.setDouble(3, amount);
            statement.setString(4, paymentDate.toString());
            statement.setString(5, paymentMonth);
            statement.setString(6, status);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        throw new SQLException("Could not read the new payment id.");
    }

    public Optional<Payment> find(int id) throws SQLException {
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT + " WHERE p.id = ?")) {
            statement.setInt(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(map(rs));
                }
                return Optional.empty();
            }
        }
    }

    public List<Payment> search(String query) throws SQLException {
        boolean filter = query != null && !query.isBlank();
        String sql = SELECT;
        if (filter) {
            sql += " WHERE m.name LIKE ? ESCAPE '\\' OR m.phone LIKE ? ESCAPE '\\' OR p.receipt_number LIKE ? ESCAPE '\\' ";
        }
        sql += " ORDER BY p.payment_date DESC, p.id DESC";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            if (filter) {
                String pattern = DatabaseManager.like(query);
                statement.setString(1, pattern);
                statement.setString(2, pattern);
                statement.setString(3, pattern);
            }
            try (ResultSet rs = statement.executeQuery()) {
                return readAll(rs);
            }
        }
    }

    public List<Payment> recent(int limit) throws SQLException {
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     SELECT + " ORDER BY p.payment_date DESC, p.id DESC LIMIT ?")) {
            statement.setInt(1, limit);
            try (ResultSet rs = statement.executeQuery()) {
                return readAll(rs);
            }
        }
    }

    public double totalCollected() throws SQLException {
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT COALESCE(SUM(amount), 0) FROM payments WHERE status = 'Paid'");
             ResultSet rs = statement.executeQuery()) {
            rs.next();
            return rs.getDouble(1);
        }
    }

    private static List<Payment> readAll(ResultSet rs) throws SQLException {
        List<Payment> rows = new ArrayList<>();
        while (rs.next()) {
            rows.add(map(rs));
        }
        return rows;
    }

    private static Payment map(ResultSet rs) throws SQLException {
        return new Payment(
                rs.getInt("id"),
                rs.getString("receipt_number"),
                rs.getInt("member_id"),
                rs.getString("member_name"),
                rs.getString("member_phone"),
                rs.getDouble("amount"),
                LocalDate.parse(rs.getString("payment_date")),
                rs.getString("payment_month"),
                rs.getString("status")
        );
    }
}
