import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
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
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class LoginPage extends JFrame {

    // --- User Components ---
    private JTextField userUsernameField;
    private JPasswordField userPasswordField;
    private JButton userLoginButton;
    private JButton userSignupButton;

    // --- Staff Login Link ---
    private JButton staffLoginLink;

 
    public static boolean isDarkMode = false;
    private JButton themeToggleButton;

    // --- UI Styling Constants ---
    // Light Theme
    private static final Color L_BACKGROUND = new Color(240, 240, 240);
    private static final Color L_PANEL = new Color(255, 255, 255);
    private static final Color L_TEXT = Color.BLACK;
    private static final Color L_BUTTON = new Color(70, 130, 180);
    private static final Color L_BUTTON_TEXT = Color.WHITE;
    // Dark Theme
 private static final Color D_BACKGROUND = new Color(30, 30, 30);
private static final Color D_PANEL = new Color(45, 45, 48);
private static final Color D_TEXT = new Color(220, 220, 220);
private static final Color D_BUTTON = new Color(0, 122, 255);
private static final Color D_BUTTON_TEXT = Color.WHITE;

    private static final Color TRANSLUCENT_PANEL_COLOR = new Color(245, 245, 245, 150); // A light, semi-transparent white


    private static final Font TITLE_FONT = new Font("Arial", Font.BOLD, 28);
    private static final Font LABEL_FONT = new Font("Arial", Font.PLAIN, 14);
    private static final Font BUTTON_FONT = new Font("Arial", Font.BOLD, 14);

    public LoginPage() {
        setTitle("Login Page");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center the frame

        // --- Set Background Image ---
        Image backgroundImage = null;
        try {
            java.net.URL imageUrl = getClass().getResource("background.jpg");
            if (imageUrl != null) {
                backgroundImage = new ImageIcon(imageUrl).getImage();
            } else {
                System.err.println("Error: background.jpg not found. Please ensure it's in the correct classpath.");
                JOptionPane.showMessageDialog(this, "Background image 'background.jpg' not found. Using solid background.", "Image Load Error", JOptionPane.WARNING_MESSAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading background image: " + e.getMessage(), "Image Load Error", JOptionPane.ERROR_MESSAGE);
        }
        ImagePanel backgroundPanel = new ImagePanel(backgroundImage, new BorderLayout(20, 20));
        backgroundPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setContentPane(backgroundPanel);

        // --- Top Panel for Title and Staff Login ---
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        // Main Title
        JLabel mainTitle = new JLabel("Salon Appointment Booking ");
        mainTitle.setFont(TITLE_FONT);
        mainTitle.setHorizontalAlignment(JLabel.CENTER);
        topPanel.add(mainTitle, BorderLayout.CENTER);

        // --- Staff Login Link ---
        staffLoginLink = new JButton("Staff Login");
        topPanel.add(staffLoginLink, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);

        // --- Center Wrapper Panel to center the User Login Panel ---
        JPanel centerWrapperPanel = new JPanel(new GridBagLayout());
        centerWrapperPanel.setOpaque(false);

        // --- User Login Panel ---
        JPanel userPanel = new JPanel();
        userPanel.setName("userLoginPanel"); // Give the panel a name to identify it for styling
        userPanel.setBorder(BorderFactory.createTitledBorder("User Login"));
        userPanel.setPreferredSize(new Dimension(350, 250)); // Set a preferred size
        userPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Add userPanel to the wrapper which will center it
        centerWrapperPanel.add(userPanel);
        add(centerWrapperPanel, BorderLayout.CENTER);

        JLabel userUsernameLabel = new JLabel("Username:");
        userUsernameLabel.setFont(LABEL_FONT);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.1;
        gbc.anchor = GridBagConstraints.EAST; // Right-align the label
        userPanel.add(userUsernameLabel, gbc);

        userUsernameField = new JTextField();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 0.9;
        gbc.anchor = GridBagConstraints.WEST; // Reset anchor for text field
        userPanel.add(userUsernameField, gbc);

        JLabel userPasswordLabel = new JLabel("Password:");
        userPasswordLabel.setFont(LABEL_FONT);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST; // Right-align the label
        userPanel.add(userPasswordLabel, gbc);

        userPasswordField = new JPasswordField();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST; // Reset anchor for text field
        userPanel.add(userPasswordField, gbc);

        // Panel for buttons to keep them together
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(15, 10, 5, 10);
        gbc.anchor = GridBagConstraints.CENTER; // Center the button panel
        userPanel.add(buttonPanel, gbc);

        userLoginButton = styleButton(new JButton("Login"));
        buttonPanel.add(userLoginButton);

        userSignupButton = styleButton(new JButton("Sign Up"));
        buttonPanel.add(userSignupButton);

        // --- Bottom Panel for Theme Toggle Button ---
        JPanel bottomPanel = new JPanel(new GridBagLayout());
        bottomPanel.setOpaque(false);

        // --- Theme Toggle Button ---
        themeToggleButton = new JButton("Toggle Theme");
        bottomPanel.add(themeToggleButton);

        add(bottomPanel, BorderLayout.SOUTH);

        // --- Action Listeners ---

        // User Login
        userLoginButton.addActionListener(e -> {
            String username = userUsernameField.getText();
            String password = new String(userPasswordField.getPassword());
            handleLogin("user", username, password);
        });

        // User Signup
        userSignupButton.addActionListener(e -> showUserSignupDialog());

        // Staff Login Link
        staffLoginLink.addActionListener(e -> showStaffLoginDialog());

        // Theme Toggle
        themeToggleButton.addActionListener(e -> {
            isDarkMode = !isDarkMode;
            applyTheme();
        });

        applyTheme(); // Apply initial theme
    }

    private void handleLogin(String userType, String username, String password) {
        String table = "";

        if (userType.equals("user")) {
            table = "users";
        } else { // staff
            table = "staff";
        }

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username and password cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Connection conn = getConnection()) {
            String sql = "SELECT * FROM " + table + " WHERE username = ? AND password = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setString(2, password); // In a real app, use hashed passwords

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String welcomeName = username;
                if (userType.equals("staff")) {
                    // For staff, use their full name for a more personal welcome
                    welcomeName = rs.getString("name");
                }

                JOptionPane.showMessageDialog(this, "Welcome, " + welcomeName + "!", "Login Successful", JOptionPane.INFORMATION_MESSAGE);

                // Capture the current window state before disposing it
                int frameState = getExtendedState();

                dispose(); // Close the login window
                if (userType.equals("user")) {
                    // Open User Dashboard
                    UserDashboard userDashboard = new UserDashboard(username);
                    if (frameState == JFrame.MAXIMIZED_BOTH) {
                        userDashboard.setExtendedState(JFrame.MAXIMIZED_BOTH);
                    }
                    userDashboard.setVisible(true);
                } else {
                    // Open Staff Dashboard
                    StaffDashboard staffDashboard = new StaffDashboard(username);
                    if (frameState == JFrame.MAXIMIZED_BOTH) {
                        staffDashboard.setExtendedState(JFrame.MAXIMIZED_BOTH);
                    }
                    staffDashboard.setVisible(true);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Invalid username or password.", "Login Failed", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error during login.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showStaffLoginDialog() {
        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();

        Object[] message = {
            "Username:", usernameField,
            "Password:", passwordField
        };

        // Create a custom panel for the dialog to hold login and signup buttons
        JPanel dialogPanel = new JPanel();
        dialogPanel.setLayout(new java.awt.GridLayout(0, 1));
        for (Object obj : message) {
            dialogPanel.add(obj instanceof java.awt.Component ? (java.awt.Component) obj : new JLabel(obj.toString()));
        }

        String[] options = {"Login", "Sign Up", "Cancel"};
        int result = JOptionPane.showOptionDialog(this, dialogPanel, "Staff Login",
                JOptionPane.NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

        if (result == 0) { // Login
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());
            handleLogin("staff", username, password);
        } else if (result == 1) { // Sign Up
            showStaffSignupDialog();
        }
        // If result is 2 (Cancel) or the dialog is closed, do nothing.
    }

    private void showUserSignupDialog() {
        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        JPasswordField confirmPasswordField = new JPasswordField();

        Object[] message = {
            "Username:", usernameField,
            "Password:", passwordField,
            "Confirm Password:", confirmPasswordField
        };

        int option = JOptionPane.showConfirmDialog(this, message, "User Signup", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());
            String confirmPassword = new String(confirmPasswordField.getPassword());

            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Username and password cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!password.equals(confirmPassword)) {
                JOptionPane.showMessageDialog(this, "Passwords do not match.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try (Connection conn = getConnection()) {
                // Check if username already exists
                String checkSql = "SELECT * FROM users WHERE username = ?";
                PreparedStatement checkStmt = conn.prepareStatement(checkSql);
                checkStmt.setString(1, username);
                if (checkStmt.executeQuery().next()) {
                    JOptionPane.showMessageDialog(this, "Username already exists.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Insert new user
                String insertSql = "INSERT INTO users (username, password) VALUES (?, ?)";
                PreparedStatement insertStmt = conn.prepareStatement(insertSql);
                insertStmt.setString(1, username);
                insertStmt.setString(2, password);
                insertStmt.executeUpdate();

                JOptionPane.showMessageDialog(this, "User signup successful! You can now log in.");
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Database error during signup.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showStaffSignupDialog() {
        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        JTextField nameField = new JTextField();
        JTextField specialisedField = new JTextField();
        JTextField availableTimeField = new JTextField("e.g., 09:00-17:00");

        Object[] message = {
            "Username:", usernameField,
            "Password:", passwordField,
            "Full Name:", nameField,
            "Specialised In:", specialisedField,
            "Available Time:", availableTimeField
        };

        int option = JOptionPane.showConfirmDialog(this, message, "Staff Signup", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());
            String name = nameField.getText();
            String specialised = specialisedField.getText();
            String availableTime = availableTimeField.getText();

            if (username.isEmpty() || password.isEmpty() || name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Username, password, and name are required.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try (Connection conn = getConnection()) {
                // Check if username already exists
                String checkSql = "SELECT * FROM staff WHERE username = ?";
                PreparedStatement checkStmt = conn.prepareStatement(checkSql);
                checkStmt.setString(1, username);
                if (checkStmt.executeQuery().next()) {
                    JOptionPane.showMessageDialog(this, "Username already exists.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                String insertSql = "INSERT INTO staff (username, password, name, specialised, available_time) VALUES (?, ?, ?, ?, ?)";
                PreparedStatement insertStmt = conn.prepareStatement(insertSql);
                insertStmt.setString(1, username);
                insertStmt.setString(2, password);
                insertStmt.setString(3, name);
                insertStmt.setString(4, specialised);
                insertStmt.setString(5, availableTime);
                insertStmt.executeUpdate();

                JOptionPane.showMessageDialog(this, "Staff signup successful! You can now log in.");
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Database error during staff signup.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void applyTheme() {
        final Color bgColor = isDarkMode ? D_BACKGROUND : L_BACKGROUND;
        final Color panelColor = isDarkMode ? D_PANEL : L_PANEL;
        final Color textColor = isDarkMode ? D_TEXT : L_TEXT;
    
        getContentPane().setBackground(bgColor);
    
        // Recursively update all components to apply the theme correctly
        updateComponentTreeUI(this, bgColor, panelColor, textColor);
    
        // Re-style buttons to apply theme
        styleButton(userLoginButton);
        styleButton(userSignupButton);
        styleButton(themeToggleButton);
        styleLink(staffLoginLink);
    }

    // Helper to recursively apply theme to all components
    private void updateComponentTreeUI(java.awt.Component c, Color background, Color panelBackground, Color foreground) {
        // Special handling for the main user login panel to keep its color fixed
        if ("userLoginPanel".equals(c.getName())) {
            c.setBackground(TRANSLUCENT_PANEL_COLOR);
            ((JPanel) c).setOpaque(true); // Ensure it's opaque
        } else if (c instanceof JPanel) {
            // Make other panels transparent to show the background image
            ((JPanel) c).setOpaque(false);
        } else if (c instanceof JLabel) {
            // If the label is inside the fixed-color login panel, it should always be dark.
            // Otherwise, it should follow the theme.
            if (c.getParent() != null && "userLoginPanel".equals(c.getParent().getName())) { // Labels inside login box
                c.setForeground(Color.BLACK);
            } else { // All other labels (like the main title)
                c.setForeground(foreground);
            }
        } else if (c instanceof JTextField || c instanceof JPasswordField) {
            // Text fields inside the login box should have a light, opaque background and black text
            c.setBackground(new Color(255, 255, 255, 200));
            c.setForeground(Color.BLACK); // Text should always be black for visibility on a light background
            ((javax.swing.text.JTextComponent) c).setCaretColor(Color.BLACK); // Cursor should also be black
        }
    
        if (c instanceof java.awt.Container) {
            for (java.awt.Component child : ((java.awt.Container) c).getComponents()) {
                // Recurse into child components
                updateComponentTreeUI(child, background, panelBackground, foreground);
            }
        }
    }

    /**
     * A helper method to apply consistent styling to buttons.
     * @param button The JButton to style.
     * @return The styled JButton.
     */
    private JButton styleButton(JButton button) {
        button.setFont(BUTTON_FONT);
        button.setBackground(isDarkMode ? D_BUTTON : L_BUTTON);
        button.setForeground(isDarkMode ? D_BUTTON_TEXT : L_BUTTON_TEXT);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));

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
                button.setBackground(isDarkMode ? D_BUTTON : L_BUTTON);
            }
        });

        return button;
    }

    /**
     * A helper method to apply styling to a link-like button.
     * @param button The JButton to style as a link.
     * @return The styled JButton.
     */
    private JButton styleLink(JButton button) {
        button.setFont(LABEL_FONT);
        button.setForeground(isDarkMode ? D_BUTTON : L_BUTTON);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        // Make it look like a link
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setOpaque(false);

        // Add hover animation
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setForeground(button.getForeground().brighter());
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setForeground(isDarkMode ? D_BUTTON : L_BUTTON);
            }
        });
        return button;
    }

    /**
     * Establishes and returns a connection to the MySQL database.
     * IMPORTANT: Update the DB_URL, DB_USER, and DB_PASSWORD with your credentials.
     * @return A Connection object to the database.
     */
    private static Connection getConnection() throws SQLException {
        // --- Database Credentials ---
        String DB_URL = "jdbc:mysql://localhost:3306/saloon";
        String DB_USER = "root"; 
        String DB_PASSWORD = "root"; 

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            // This makes the error clearer if the JDBC driver is missing
            throw new SQLException("MySQL JDBC Driver not found.", e);
        }
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
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
            // Draw the background image, scaling it to fill the panel
            if (backgroundImage != null) {
                g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
            }
        }
    }

    public static void main(String[] args) {
        // Run the application
        try {
            // Set Nimbus Look and Feel for a more modern UI
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) { /* If Nimbus is not available, fall back to default */ }
        SwingUtilities.invokeLater(() -> new LoginPage().setVisible(true)); // Run the application
    }
}
