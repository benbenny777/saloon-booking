import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.LayoutManager;
import java.awt.event.MouseAdapter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

public class StaffDashboard extends JFrame {

    private String loggedInStaffUsername;
    private String loggedInStaffName; // To be fetched from DB

    // --- UI Styling Constants ---
    // Light Theme
    private static final Color L_BACKGROUND = new Color(240, 240, 240);
    private static final Color L_BUTTON = new Color(0, 128, 128); // Teal
    private static final Color L_BUTTON_TEXT = Color.WHITE; // Consistent with other pages
    // Dark Theme
    private static final Color D_BACKGROUND = new Color(30, 30, 30); // Consistent with other pages
    private static final Color D_BUTTON = new Color(0, 122, 255);
    private static final Color D_BUTTON_TEXT = Color.WHITE;

    private static final Font BUTTON_FONT = new Font("Arial", Font.BOLD, 14); // Smaller font for buttons
    private static final Font WELCOME_FONT = new Font("Arial", Font.BOLD, 24);

    // --- Components ---
    private JButton viewAppointmentsButton;
    private JButton updateProfileButton;
    private JButton deleteAccountButton;
    private JButton logoutButton;
    private JButton themeToggleButton;
    private JLabel welcomeLabel;

    // A different color for the delete button to indicate caution
    private static final Color DELETE_BUTTON_COLOR = new Color(220, 20, 60); // Crimson

    public StaffDashboard(String username) {
        this.loggedInStaffUsername = username;
        fetchStaffName(); // Get the staff's full name for queries

        setTitle("Staff Dashboard - Welcome " + loggedInStaffName);
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // --- Set Background Image ---
        Image backgroundImage = null;
        try {
            java.net.URL imageUrl = getClass().getResource("bk.jpg"); // Consistent with user's request
            if (imageUrl != null) {
                backgroundImage = new ImageIcon(imageUrl).getImage();
            } else {
                System.err.println("Error: 'blackbackground.jpg' not found. Please ensure it's in the correct classpath.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        ImagePanel backgroundPanel = new ImagePanel(backgroundImage, new BorderLayout(20, 20));
        backgroundPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setContentPane(backgroundPanel);
        // --- Header Panel ---
        welcomeLabel = new JLabel("Welcome, " + loggedInStaffName);
        welcomeLabel.setFont(WELCOME_FONT);
        welcomeLabel.setHorizontalAlignment(JLabel.CENTER);
        add(welcomeLabel, BorderLayout.NORTH);

        // --- Center Panel for Buttons ---
        // This outer panel uses GridBagLayout to center the inner button panel
        JPanel centerWrapperPanel = new JPanel(new GridBagLayout());
        // This panel holds the buttons in a vertical list
        JPanel buttonPanel = new JPanel(new GridLayout(0, 1, 15, 15));

        // Add some padding to the button container to give it more width and breathing room
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        viewAppointmentsButton = new JButton("View My Appointments");
        updateProfileButton = new JButton("Update My Profile");
        deleteAccountButton = new JButton("Delete My Account");
        logoutButton = new JButton("Logout");
        themeToggleButton = new JButton("Toggle Theme");

        buttonPanel.add(viewAppointmentsButton);
        buttonPanel.add(updateProfileButton);
        buttonPanel.add(deleteAccountButton);
        buttonPanel.add(logoutButton);
        buttonPanel.add(themeToggleButton);

        centerWrapperPanel.add(buttonPanel); // Add the button column to the centering wrapper
        add(centerWrapperPanel, BorderLayout.CENTER);

        // --- Action Listeners ---
        viewAppointmentsButton.addActionListener(e -> viewAppointments());
        updateProfileButton.addActionListener(e -> showUpdateProfileDialog());
        deleteAccountButton.addActionListener(e -> deleteAccount());
        logoutButton.addActionListener(e -> logout());

        themeToggleButton.addActionListener(e -> {
            LoginPage.isDarkMode = !LoginPage.isDarkMode;
            applyTheme();
        });

        applyTheme(); // Apply initial theme
    }

    /**
     * Fetches the staff member's full name from the database using their username.
     * The full name is used to query the bookings table.
     */
    private void fetchStaffName() {
        try (Connection conn = getConnection()) {
            String sql = "SELECT name FROM staff WHERE username = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, loggedInStaffUsername);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                this.loggedInStaffName = rs.getString("name");
            } else {
                // Fallback if name not found, though this shouldn't happen if login was successful
                this.loggedInStaffName = loggedInStaffUsername;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            this.loggedInStaffName = loggedInStaffUsername; // Fallback on error
            JOptionPane.showMessageDialog(this, "Could not fetch staff details.", "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void viewAppointments() {
        int originalState = getExtendedState();
        try (Connection conn = getConnection()) {
            String sql = "SELECT id, name, phone, gender, cutting_type AS Service, date, time FROM bookings WHERE staff_name = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, this.loggedInStaffName);
            ResultSet rs = stmt.executeQuery();
 
            // Using the same utility method from UserDashboard to build the table
            DefaultTableModel tableModel = UserDashboard.buildTableModel(rs);
            if (tableModel.getRowCount() == 0) {
                JOptionPane.showMessageDialog(this, "You have no appointments assigned to you.", "My Appointments", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
 
            JTable table = new JTable(tableModel);
            JScrollPane scrollPane = new JScrollPane(table);
            scrollPane.setPreferredSize(new Dimension(750, 400));
 
            JOptionPane.showMessageDialog(this, scrollPane, "My Assigned Appointments", JOptionPane.INFORMATION_MESSAGE);
 
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error fetching your appointments.", "Database Error", JOptionPane.ERROR_MESSAGE);
        } finally { // Ensure state is re-applied even if an error occurs
            if (originalState == JFrame.MAXIMIZED_BOTH) {
                setExtendedState(JFrame.MAXIMIZED_BOTH);
            }
        }
    }

    private void showUpdateProfileDialog() {
        int originalState = getExtendedState();
        // --- Components for the dialog ---
        JTextField nameField = new JTextField();
        JTextField specialisedField = new JTextField();
        JTextField availableTimeField = new JTextField();

        // --- Pre-fill with current data ---
        try (Connection conn = getConnection()) {
            String sql = "SELECT name, specialised, available_time FROM staff WHERE username = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, loggedInStaffUsername);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                nameField.setText(rs.getString("name"));
                specialisedField.setText(rs.getString("specialised"));
                availableTimeField.setText(rs.getString("available_time"));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error fetching profile data.", "Database Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Object[] message = {
                "Full Name:", nameField,
                "Specialised In:", specialisedField,
                "Available Time (e.g., 09:00-17:00):", availableTimeField
        };

        int option = JOptionPane.showConfirmDialog(this, message, "Update Profile", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String newName = nameField.getText();
            String newSpecialised = specialisedField.getText();
            String newAvailableTime = availableTimeField.getText();

            if (newName.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Name cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try (Connection conn = getConnection()) {
                String sql = "UPDATE staff SET name = ?, specialised = ?, available_time = ? WHERE username = ?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, newName);
                stmt.setString(2, newSpecialised);
                stmt.setString(3, newAvailableTime);
                stmt.setString(4, loggedInStaffUsername);

                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(this, "Profile updated successfully!");
                    // Update the name used for display and queries
                    this.loggedInStaffName = newName;
                    setTitle("Staff Dashboard - Welcome " + this.loggedInStaffName);
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to update profile.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Database error during profile update.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
        // Re-apply state after the dialog interaction
        if (originalState == JFrame.MAXIMIZED_BOTH) {
            setExtendedState(JFrame.MAXIMIZED_BOTH);
        }
    }

    private void deleteAccount() {
        int originalState = getExtendedState();
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete your account?\nThis action cannot be undone.",
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = getConnection()) {
                // Note: In a real-world app, you might want to handle existing appointments
                // (e.g., reassign them or notify users) before deleting a staff member.
                // For this project, we will just delete the staff record.

                String sql = "DELETE FROM staff WHERE username = ?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, loggedInStaffUsername);

                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(this, "Account deleted successfully.");
                    // Close dashboard and open a new login page
                    dispose();
                    new LoginPage().setVisible(true);
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to delete account.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Database error during account deletion.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
        // Re-apply state if user cancels or an error occurs before dispose()
        if (originalState == JFrame.MAXIMIZED_BOTH) {
            setExtendedState(JFrame.MAXIMIZED_BOTH);
        }
    }

    private void logout() {
        // Capture the current window state
        int frameState = getExtendedState();

        // Create and configure the new login page
        LoginPage loginPage = new LoginPage();
        if (frameState == JFrame.MAXIMIZED_BOTH) {
            loginPage.setExtendedState(JFrame.MAXIMIZED_BOTH);
        }
        loginPage.setVisible(true); // Show the new page
        dispose(); // Close the current dashboard window
    }

    private void applyTheme() {
        boolean isDarkMode = LoginPage.isDarkMode;
        // The background is an image, so we don't set a color.
        // We just need to make sure text is visible on the dark image.
        welcomeLabel.setForeground(Color.WHITE);

        // Apply to panels
        for (java.awt.Component comp : getContentPane().getComponents()) {
            if (comp instanceof JPanel) {
                // Make all panels transparent to show the background image
                ((JPanel) comp).setOpaque(false);
                // Also make their sub-panels transparent or semi-transparent
                for (java.awt.Component subComp : ((JPanel) comp).getComponents()) {
                    if (subComp instanceof JPanel) {
                        subComp.setBackground(new Color(45, 45, 48, 170)); // Semi-transparent dark background
                        ((JPanel) subComp).setOpaque(true); // Make the panel paint its background
                    }
                }
            }
        }
        // Re-style buttons for the current theme
        styleButton(viewAppointmentsButton);
        styleButton(updateProfileButton);
        styleButton(deleteAccountButton);
        styleButton(logoutButton);
        styleButton(themeToggleButton);
    }

    /**
     * A custom JPanel that can display a background image.
     */
    private static class ImagePanel extends JPanel {
        private Image backgroundImage;

        public ImagePanel(Image backgroundImage, LayoutManager layoutManager) {
            super(layoutManager);
            this.backgroundImage = backgroundImage;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (backgroundImage != null) {
                g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
            }
        }
    }

    /**
     * A helper method to apply consistent styling to buttons.
     * @param button The JButton to style.
     * @return The styled JButton.
     */
    private JButton styleButton(JButton button) {
        // Special color for delete button
        if (button == deleteAccountButton) {
            button.setBackground(DELETE_BUTTON_COLOR);
        } else {
            button.setBackground(LoginPage.isDarkMode ? D_BUTTON : new Color(70, 130, 180)); // Consistent L_BUTTON
        }
        button.setFont(BUTTON_FONT);
        button.setForeground(LoginPage.isDarkMode ? D_BUTTON_TEXT : L_BUTTON_TEXT);
        button.setFocusPainted(false);

        // Set a simple padding border without a line for a flatter look
        button.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));

        // Remove old hover listeners to prevent duplicates and ensure clicks work
        for (java.awt.event.MouseListener ml : button.getMouseListeners()) {
            if (ml instanceof MouseAdapter) {
                button.removeMouseListener(ml);
            }
        }

        // Add hover animation
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(button.getBackground().brighter());
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (button == deleteAccountButton) {
                    button.setBackground(DELETE_BUTTON_COLOR);
                } else {
                    button.setBackground(LoginPage.isDarkMode ? D_BUTTON : new Color(70, 130, 180)); // Consistent L_BUTTON
                }
            }
        });

        return button;
    }

    /**
     * Establishes and returns a connection to the MySQL database.
     * This is a helper method, similar to the one in other classes.
     */
    private static Connection getConnection() throws SQLException {
        String DB_URL = "jdbc:mysql://localhost:3306/saloon";
        String DB_USER = "root";
        String DB_PASSWORD = "root";

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC Driver not found.", e);
        }
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }
}