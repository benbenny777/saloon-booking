import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.LayoutManager;
import java.awt.event.MouseAdapter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

public class UserDashboard extends JFrame {

    private String loggedInUsername;

    // --- UI Styling Constants ---
    // Light Theme
    private static final Color L_BACKGROUND = new Color(240, 240, 240);
    private static final Color L_BUTTON = new Color(70, 130, 180);
    private static final Color L_BUTTON_TEXT = Color.WHITE;
    // Dark Theme
    private static final Color D_BACKGROUND = new Color(30, 30, 30);
    private static final Color D_BUTTON = new Color(0, 122, 255);
    private static final Color D_BUTTON_TEXT = Color.WHITE;

    private static final Font TITLE_FONT = new Font("Arial", Font.BOLD, 28);
    private static final Font WELCOME_FONT = new Font("Arial", Font.BOLD, 24); // A dedicated font for the welcome message
    private static final Font BUTTON_FONT = new Font("Arial", Font.BOLD, 14);

    // --- Components ---
    private JButton bookButton;
    private JLabel titleLabel; // Make title label a class member to change its color
    private JButton updateButton;
    private JButton displayButton;
    private JButton deleteButton;
    private JButton logoutButton;
    private JButton themeToggleButton;

    public UserDashboard(String username) {
        this.loggedInUsername = username;

        setTitle("User Dashboard - Welcome " + loggedInUsername);
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // --- Set Background Image ---
       Image backgroundImage = null;
        try {
            java.net.URL imageUrl = getClass().getResource("bk.jpg");
            if (imageUrl != null) {
                backgroundImage = new ImageIcon(imageUrl).getImage();
            } else {
                System.err.println("Error: blackbackground.jpg not found. Please ensure it's in the correct classpath.");
                JOptionPane.showMessageDialog(this, "Background image 'blackbackground.jpg' not found. Using solid background.", "Image Load Error", JOptionPane.WARNING_MESSAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading 'blackbackground.jpg': " + e.getMessage(), "Image Load Error", JOptionPane.ERROR_MESSAGE);
        }
        ImagePanel backgroundPanel = new ImagePanel(backgroundImage, new BorderLayout(20, 20));
        backgroundPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setContentPane(backgroundPanel);

        // --- Header Panel ---
        titleLabel = new JLabel("Welcome, " + loggedInUsername);
        titleLabel.setFont(WELCOME_FONT);
        titleLabel.setHorizontalAlignment(JLabel.CENTER);
        add(titleLabel, BorderLayout.NORTH);

        // --- Center Panel to hold and center the buttons ---
        // This outer panel uses GridBagLayout to center the inner button panel
        JPanel centerWrapperPanel = new JPanel(new java.awt.GridBagLayout());

        // This inner panel holds the buttons in a neat vertical column
        JPanel buttonColumnPanel = new JPanel(new GridLayout(0, 1, 15, 15));

        // Add some padding to the button container to give it more width and breathing room
        buttonColumnPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        bookButton = new JButton("Book Appointment");
        updateButton = new JButton("Update Appointment");
        displayButton = new JButton("Display My Bookings");
        deleteButton = new JButton("Delete Booking");

        buttonColumnPanel.add(bookButton);
        buttonColumnPanel.add(updateButton);
        buttonColumnPanel.add(displayButton);
        buttonColumnPanel.add(deleteButton);

        centerWrapperPanel.add(buttonColumnPanel); // Add the button column to the centering wrapper
        add(centerWrapperPanel, BorderLayout.CENTER);

        // --- Bottom Panel for Utility Buttons ---
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));

        logoutButton = new JButton("Logout");
        themeToggleButton = new JButton("Toggle Theme");

        bottomPanel.add(logoutButton);
        bottomPanel.add(themeToggleButton);

        add(bottomPanel, BorderLayout.SOUTH);

        // --- Action Listeners ---

        bookButton.addActionListener(e -> showBookingDialog());
        displayButton.addActionListener(e -> displayBookings());
        updateButton.addActionListener(e -> showUpdateDialog());
        deleteButton.addActionListener(e -> showDeleteDialog());
        logoutButton.addActionListener(e -> logout());

        themeToggleButton.addActionListener(e -> {
            LoginPage.isDarkMode = !LoginPage.isDarkMode;
            applyTheme();
        });

        applyTheme(); // Apply initial theme

    }

    private void showBookingDialog() {
        int originalState = getExtendedState();
        // --- Form Components ---
        JTextField phoneField = new JTextField();
        JTextField nameField = new JTextField();
        JComboBox<String> genderComboBox = new JComboBox<>(new String[]{"Male", "Female", "Other"});
        JComboBox<String> staffComboBox = new JComboBox<>();
        JTextField serviceField = new JTextField("eg:haircutting");
        JTextField timeField = new JTextField("HH:MM");
        JTextField dateField = new JTextField("YYYY-MM-DD");

        // --- Populate Staff ComboBox ---
        try (Connection conn = getConnection()) { // This will throw an exception if connection fails
            String sql = "SELECT name, specialised, available_time FROM staff";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                String staffInfo = String.format("%s (%s, %s)",
                        rs.getString("name"),
                        rs.getString("specialised"),
                        rs.getString("available_time"));
                staffComboBox.addItem(staffInfo);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error fetching staff data.", "Database Error", JOptionPane.ERROR_MESSAGE);
        }

        Object[] message = {
                "Phone No:", phoneField,
                "Name:", nameField,
                "Gender:", genderComboBox,
                "Staff:", staffComboBox,
                "Service:", serviceField,
                "Time (24h format):", timeField,
                "Date (YYYY-MM-DD):", dateField
        };

        int option = JOptionPane.showConfirmDialog(this, message, "Book Appointment", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String phone = phoneField.getText();
            String name = nameField.getText();
            String gender = (String) genderComboBox.getSelectedItem();
            String staffInfo = (String) staffComboBox.getSelectedItem();
            // Extract only the name from the staff info string
            String staffName = staffInfo.split(" \\(")[0];
            String service = serviceField.getText();
            String time = timeField.getText();
            String date = dateField.getText();

            if (name.isEmpty() || phone.isEmpty() || time.isEmpty() || date.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill all required fields.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // --- Check for booking conflicts ---
            try (Connection conn = getConnection()) {
                String checkSql = "SELECT * FROM bookings WHERE staff_name = ? AND date = ? AND time = ?";
                PreparedStatement checkStmt = conn.prepareStatement(checkSql);
                checkStmt.setString(1, staffName);
                checkStmt.setString(2, date);
                checkStmt.setString(3, time);

                if (checkStmt.executeQuery().next()) {
                    JOptionPane.showMessageDialog(this, "Slot filled. This staff is already booked at this time.", "Booking Conflict", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // --- Insert new booking ---
                String insertSql = "INSERT INTO bookings (username, name, phone, gender, staff_name, cutting_type, date, time) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                PreparedStatement insertStmt = conn.prepareStatement(insertSql);
                insertStmt.setString(1, loggedInUsername);
                insertStmt.setString(2, name);
                insertStmt.setString(3, phone);
                insertStmt.setString(4, gender);
                insertStmt.setString(5, staffName);
                insertStmt.setString(6, service);
                insertStmt.setString(7, date);
                insertStmt.setString(8, time);

                int rowsAffected = insertStmt.executeUpdate();
                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(this, "Appointment booked successfully!");
                }

            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Database error during booking.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void displayBookings() {
        int originalState = getExtendedState();
        try (Connection conn = getConnection()) {
            String sql = "SELECT id, name, phone, gender, staff_name, cutting_type AS Service, date, time FROM bookings WHERE username = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, loggedInUsername);
            ResultSet rs = stmt.executeQuery();

            // Create a table model and JTable
            DefaultTableModel tableModel = buildTableModel(rs);
            if (tableModel.getRowCount() == 0) {
                JOptionPane.showMessageDialog(this, "You have no bookings.", "My Bookings", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            JTable table = new JTable(tableModel);
            JScrollPane scrollPane = new JScrollPane(table);
            scrollPane.setPreferredSize(new Dimension(750, 400));

            // Show the table in a dialog
            JOptionPane.showMessageDialog(this, scrollPane, "My Bookings", JOptionPane.INFORMATION_MESSAGE);

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error fetching your bookings.", "Database Error", JOptionPane.ERROR_MESSAGE);
        } finally { // Ensure state is re-applied even if an error occurs
            if (originalState == JFrame.MAXIMIZED_BOTH) {
                setExtendedState(JFrame.MAXIMIZED_BOTH);
            }
        }
    }

    private void showUpdateDialog() {
        int originalState = getExtendedState();
        String bookingIdStr = JOptionPane.showInputDialog(this, "Enter the Booking ID to update:");
        if (bookingIdStr == null || bookingIdStr.trim().isEmpty()) {
            return;
        }

        try {
            int bookingId = Integer.parseInt(bookingIdStr);
            String newTime = JOptionPane.showInputDialog(this, "Enter new time (HH:MM):");
            String newDate = JOptionPane.showInputDialog(this, "Enter new date (YYYY-MM-DD):");

            if ((newTime == null || newTime.trim().isEmpty()) && (newDate == null || newDate.trim().isEmpty())) {
                JOptionPane.showMessageDialog(this, "No changes provided.", "Update Canceled", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            try (Connection conn = getConnection()) {
                // First, verify the booking belongs to the user
                String verifySql = "SELECT staff_name FROM bookings WHERE id = ? AND username = ?";
                PreparedStatement verifyStmt = conn.prepareStatement(verifySql);
                verifyStmt.setInt(1, bookingId);
                verifyStmt.setString(2, loggedInUsername);
                ResultSet rs = verifyStmt.executeQuery();

                if (!rs.next()) {
                    JOptionPane.showMessageDialog(this, "Booking ID not found or you don't have permission to update it.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                String staffName = rs.getString("staff_name");

                // Build the update query dynamically
                StringBuilder updateSql = new StringBuilder("UPDATE bookings SET ");
                ArrayList<String> params = new ArrayList<>();
                if (newTime != null && !newTime.trim().isEmpty()) {
                    updateSql.append("time = ? ");
                    params.add(newTime);
                }
                if (newDate != null && !newDate.trim().isEmpty()) {
                    if (!params.isEmpty()) updateSql.append(", ");
                    updateSql.append("date = ? ");
                    params.add(newDate);
                }
                updateSql.append("WHERE id = ?");

                // Check for booking conflicts before updating
                String checkSql = "SELECT * FROM bookings WHERE staff_name = ? AND date = ? AND time = ? AND id != ?";
                PreparedStatement checkStmt = conn.prepareStatement(checkSql);
                checkStmt.setString(1, staffName);
                checkStmt.setString(2, (newDate != null && !newDate.isEmpty()) ? newDate : getCurrentBookingValue(conn, bookingId, "date"));
                checkStmt.setString(3, (newTime != null && !newTime.isEmpty()) ? newTime : getCurrentBookingValue(conn, bookingId, "time"));
                checkStmt.setInt(4, bookingId);

                if (checkStmt.executeQuery().next()) {
                    JOptionPane.showMessageDialog(this, "Slot filled. This staff is already booked at the new time/date.", "Booking Conflict", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // Execute the update
                PreparedStatement updateStmt = conn.prepareStatement(updateSql.toString());
                int paramIndex = 1;
                for (String param : params) {
                    updateStmt.setString(paramIndex++, param);
                }
                updateStmt.setInt(paramIndex, bookingId);

                int rowsAffected = updateStmt.executeUpdate();
                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(this, "Booking updated successfully!");
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to update booking.", "Error", JOptionPane.ERROR_MESSAGE);
                }

            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Database error during update.", "Error", JOptionPane.ERROR_MESSAGE);
            }

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid Booking ID. Please enter a number.", "Input Error", JOptionPane.ERROR_MESSAGE);
        } finally { // Ensure state is re-applied even if an error occurs
            if (originalState == JFrame.MAXIMIZED_BOTH) {
                setExtendedState(JFrame.MAXIMIZED_BOTH);
            }
        }
    }

    // Helper to get current value if only one field is being updated
    private String getCurrentBookingValue(Connection conn, int bookingId, String columnName) throws SQLException {
        String sql = "SELECT " + columnName + " FROM bookings WHERE id = ?";
        // This is a read-only operation within a larger dialog flow,
        // so we don't need to manage the frame state here. The calling
        // method (showUpdateDialog) is responsible for that.

        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, bookingId);
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            return rs.getString(columnName);
        }
        return "";
    }

    private void showDeleteDialog() {
        int originalState = getExtendedState();
        String bookingIdStr = JOptionPane.showInputDialog(this, "Enter the Booking ID to delete:");
        if (bookingIdStr == null || bookingIdStr.trim().isEmpty()) {
            return;
        }

        try {
            int bookingId = Integer.parseInt(bookingIdStr);
            int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete booking ID " + bookingId + "?", "Confirm Deletion", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                try (Connection conn = getConnection()) {
                    String sql = "DELETE FROM bookings WHERE id = ? AND username = ?";
                    PreparedStatement stmt = conn.prepareStatement(sql);
                    stmt.setInt(1, bookingId);
                    stmt.setString(2, loggedInUsername);

                    int rowsAffected = stmt.executeUpdate();
                    if (rowsAffected > 0) {
                        JOptionPane.showMessageDialog(this, "Booking deleted successfully!");
                    } else {
                        JOptionPane.showMessageDialog(this, "Booking ID not found or you don't have permission to delete it.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Database error during deletion.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid Booking ID. Please enter a number.", "Input Error", JOptionPane.ERROR_MESSAGE);
        } finally { // Ensure state is re-applied even if an error occurs
            if (originalState == JFrame.MAXIMIZED_BOTH) {
                setExtendedState(JFrame.MAXIMIZED_BOTH);
            }
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
        titleLabel.setForeground(Color.WHITE); 

        // Apply to panels
        for (java.awt.Component comp : getContentPane().getComponents()) {
            if (comp instanceof JPanel) {
                ((JPanel) comp).setOpaque(false);
                for (java.awt.Component subComp : ((JPanel) comp).getComponents()) {
                    if (subComp instanceof JPanel) {
                        subComp.setBackground(new Color(45, 45, 48, 170)); // Semi-transparent dark background
                        ((JPanel) subComp).setOpaque(true); // Make the panel paint its background
                    }
                }
            }
        }

        // Re-style buttons for the current theme
        styleButton(bookButton);
        styleButton(updateButton);
        styleButton(displayButton);
        styleButton(deleteButton);
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
     * Utility method to convert a ResultSet to a DefaultTableModel.
     */
    public static DefaultTableModel buildTableModel(ResultSet rs) throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();

        // names of columns
        Vector<String> columnNames = new Vector<>();
        int columnCount = metaData.getColumnCount();
        for (int column = 1; column <= columnCount; column++) {
            columnNames.add(metaData.getColumnName(column));
        }

        // data of the table
        Vector<Vector<Object>> data = new Vector<>();
        while (rs.next()) {
            Vector<Object> vector = new Vector<>();
            for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
                vector.add(rs.getObject(columnIndex));
            }
            data.add(vector);
        }

        return new DefaultTableModel(data, columnNames);
    }

    /**
     * A helper method to apply consistent styling to buttons.
     * @param button The JButton to style.
     * @return The styled JButton.
     */
    private JButton styleButton(JButton button) {
        button.setFont(BUTTON_FONT);
        button.setBackground(LoginPage.isDarkMode ? D_BUTTON : L_BUTTON);
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
                button.setBackground(LoginPage.isDarkMode ? D_BUTTON : L_BUTTON);
            }
        });

        return button;
    }

    /**
     * Establishes and returns a connection to the MySQL database.
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