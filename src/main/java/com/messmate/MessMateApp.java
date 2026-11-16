package com.messmate;

import com.messmate.db.DatabaseManager;
import com.messmate.ui.LoginFrame;
import com.messmate.ui.Ui;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.nio.file.Path;

public final class MessMateApp {
    private MessMateApp() {
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Ui.installLook();
            try {
                Path database = DatabaseManager.defaultDatabasePath();
                DatabaseManager.initialize(database);
                System.out.println("MessMate database: " + database.toAbsolutePath());
                new LoginFrame().setVisible(true);
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(null,
                        "MessMate could not start.\n" + ex.getMessage(),
                        "MessMate",
                        JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        });
    }
}
