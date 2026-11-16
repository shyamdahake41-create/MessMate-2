package com.messmate.service;

import com.messmate.db.AdminDao;
import com.messmate.util.AppException;
import com.messmate.util.PasswordHasher;

import java.sql.SQLException;

public class AuthService {
    private final AdminDao adminDao;

    public AuthService() {
        this(new AdminDao());
    }

    public AuthService(AdminDao adminDao) {
        this.adminDao = adminDao;
    }

    public boolean login(String username, String password) {
        if (username == null || password == null || username.isBlank() || password.isBlank()) {
            return false;
        }
        try {
            return adminDao.matches(username.trim(), PasswordHasher.hash(password));
        } catch (SQLException ex) {
            ex.printStackTrace();
            throw new AppException("Could not verify the login. Please try again.");
        }
    }
}
