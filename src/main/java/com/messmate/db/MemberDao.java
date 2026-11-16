package com.messmate.db;

import com.messmate.model.Member;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MemberDao {
    public int insert(Member member) throws SQLException {
        String sql = """
                INSERT INTO members(name, phone, address, joining_date, monthly_fee)
                VALUES (?, ?, ?, ?, ?)
                """;
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            bind(statement, member);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        throw new SQLException("Could not read the new member id.");
    }

    public void update(Member member) throws SQLException {
        String sql = """
                UPDATE members
                SET name = ?, phone = ?, address = ?, joining_date = ?, monthly_fee = ?
                WHERE id = ?
                """;
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            bind(statement, member);
            statement.setInt(6, member.getId());
            if (statement.executeUpdate() == 0) {
                throw new SQLException("Member not found.");
            }
        }
    }

    public int delete(int id) throws SQLException {
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM members WHERE id = ?")) {
            statement.setInt(1, id);
            return statement.executeUpdate();
        }
    }

    public Optional<Member> find(int id) throws SQLException {
        try (Connection connection = DatabaseManager.getConnection()) {
            return find(connection, id);
        }
    }

    public Optional<Member> find(Connection connection, int id) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("""
                SELECT id, name, phone, address, joining_date, monthly_fee
                FROM members WHERE id = ?
                """)) {
            statement.setInt(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(map(rs));
                }
                return Optional.empty();
            }
        }
    }

    public boolean exists(Connection connection, int id) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("SELECT 1 FROM members WHERE id = ?")) {
            statement.setInt(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next();
            }
        }
    }

    public int count() throws SQLException {
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) FROM members");
             ResultSet rs = statement.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        }
    }

    public List<Member> search(String query) throws SQLException {
        boolean filter = query != null && !query.isBlank();
        String sql = """
                SELECT id, name, phone, address, joining_date, monthly_fee
                FROM members
                """;
        if (filter) {
            sql += " WHERE name LIKE ? ESCAPE '\\' OR phone LIKE ? ESCAPE '\\' ";
        }
        sql += " ORDER BY name COLLATE NOCASE, id";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            if (filter) {
                String pattern = DatabaseManager.like(query);
                statement.setString(1, pattern);
                statement.setString(2, pattern);
            }
            try (ResultSet rs = statement.executeQuery()) {
                List<Member> members = new ArrayList<>();
                while (rs.next()) {
                    members.add(map(rs));
                }
                return members;
            }
        }
    }

    private static void bind(PreparedStatement statement, Member member) throws SQLException {
        statement.setString(1, member.getName());
        statement.setString(2, member.getPhone());
        statement.setString(3, member.getAddress());
        statement.setString(4, member.getJoiningDate().toString());
        statement.setDouble(5, member.getMonthlyFee());
    }

    private static Member map(ResultSet rs) throws SQLException {
        return new Member(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("phone"),
                rs.getString("address"),
                LocalDate.parse(rs.getString("joining_date")),
                rs.getDouble("monthly_fee")
        );
    }
}
