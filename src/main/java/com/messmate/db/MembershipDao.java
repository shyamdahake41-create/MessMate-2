package com.messmate.db;

import com.messmate.model.Membership;
import com.messmate.model.MembershipStatus;
import com.messmate.util.DateRanges;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MembershipDao {
    private static final String SELECT = """
            SELECT ms.id, ms.member_id, m.name AS member_name, m.phone AS member_phone,
                   ms.start_date, ms.expiry_date, ms.monthly_fee
            FROM memberships ms
            JOIN members m ON m.id = ms.member_id
            """;

    public int insert(int memberId, LocalDate start, LocalDate expiry, double fee) throws SQLException {
        String sql = """
                INSERT INTO memberships(member_id, start_date, expiry_date, monthly_fee)
                VALUES (?, ?, ?, ?)
                """;
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, memberId);
            statement.setString(2, start.toString());
            statement.setString(3, expiry.toString());
            statement.setDouble(4, fee);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        throw new SQLException("Could not read the new membership id.");
    }

    public Optional<Membership> find(int id) throws SQLException {
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT + " WHERE ms.id = ?")) {
            statement.setInt(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(map(rs));
                }
                return Optional.empty();
            }
        }
    }

    public List<Membership> listAll() throws SQLException {
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT + " ORDER BY ms.expiry_date DESC, ms.id DESC");
             ResultSet rs = statement.executeQuery()) {
            return readAll(rs);
        }
    }

    public List<Membership> listByMember(int memberId) throws SQLException {
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT + " WHERE ms.member_id = ? ORDER BY ms.expiry_date DESC")) {
            statement.setInt(1, memberId);
            try (ResultSet rs = statement.executeQuery()) {
                return readAll(rs);
            }
        }
    }

    public Optional<Membership> latest(int memberId) throws SQLException {
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     SELECT + " WHERE ms.member_id = ? ORDER BY ms.expiry_date DESC, ms.id DESC LIMIT 1")) {
            statement.setInt(1, memberId);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(map(rs));
                }
                return Optional.empty();
            }
        }
    }

    public int countActive(LocalDate today) throws SQLException {
        return count("""
                SELECT COUNT(*) FROM memberships
                WHERE start_date <= ? AND expiry_date >= ?
                """, today.toString(), today.toString());
    }

    public int countExpired(LocalDate today) throws SQLException {
        return count("SELECT COUNT(*) FROM memberships WHERE expiry_date < ?", today.toString());
    }

    private int count(String sql, String... params) throws SQLException {
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                statement.setString(i + 1, params[i]);
            }
            try (ResultSet rs = statement.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    private static List<Membership> readAll(ResultSet rs) throws SQLException {
        List<Membership> rows = new ArrayList<>();
        while (rs.next()) {
            rows.add(map(rs));
        }
        return rows;
    }

    private static Membership map(ResultSet rs) throws SQLException {
        LocalDate start = LocalDate.parse(rs.getString("start_date"));
        LocalDate expiry = LocalDate.parse(rs.getString("expiry_date"));
        MembershipStatus status = DateRanges.status(start, expiry, LocalDate.now());
        return new Membership(
                rs.getInt("id"),
                rs.getInt("member_id"),
                rs.getString("member_name"),
                rs.getString("member_phone"),
                start,
                expiry,
                rs.getDouble("monthly_fee"),
                status.label()
        );
    }
}
