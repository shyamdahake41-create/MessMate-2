package com.messmate.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AdminDao {
    public boolean matches(String username, String passwordHash) throws SQLException {
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT 1 FROM admins WHERE username = ? AND password_hash = ?")) {
            statement.setString(1, username);
            statement.setString(2, passwordHash);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next();
            }
        }
    }
}
