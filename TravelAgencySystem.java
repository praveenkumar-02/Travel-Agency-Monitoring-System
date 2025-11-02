import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class TravelAgencySystem extends Frame implements ActionListener {

    Label title, nameLabel, destLabel, dateLabel, priceLabel, modeLabel, resultLabel;
    TextField nameField, destField, dateField, priceField;
    Choice modeChoice;
    Button bookBtn, resetBtn, exitBtn, showBtn;
    TextArea displayArea;

    Connection conn;
    PreparedStatement pstmt;
    Statement stmt;

    public TravelAgencySystem() {

        // Frame settings
        setTitle("Traveling Agency Monitoring System");
        setSize(700, 600);
        setLayout(null);
        setBackground(new Color(230, 240, 255));
        setVisible(true);

        // Title
        title = new Label("Travel Agency Booking System");
        title.setFont(new Font("Arial", Font.BOLD, 20));
        title.setBounds(180, 50, 400, 30);
        add(title);

        // Labels & Fields
        nameLabel = new Label("Customer Name:");
        nameLabel.setBounds(100, 100, 120, 25);
        add(nameLabel);

        nameField = new TextField();
        nameField.setBounds(230, 100, 250, 25);
        add(nameField);

        destLabel = new Label("Destination:");
        destLabel.setBounds(100, 140, 120, 25);
        add(destLabel);

        destField = new TextField();
        destField.setBounds(230, 140, 250, 25);
        add(destField);

        dateLabel = new Label("Travel Date:");
        dateLabel.setBounds(100, 180, 120, 25);
        add(dateLabel);

        dateField = new TextField("DD/MM/YYYY");
        dateField.setBounds(230, 180, 250, 25);
        add(dateField);

        priceLabel = new Label("Ticket Price (₹):");
        priceLabel.setBounds(100, 220, 120, 25);
        add(priceLabel);

        priceField = new TextField();
        priceField.setBounds(230, 220, 250, 25);
        add(priceField);

        modeLabel = new Label("Mode of Travel:");
        modeLabel.setBounds(100, 260, 120, 25);
        add(modeLabel);

        modeChoice = new Choice();
        modeChoice.add("Flight");
        modeChoice.add("Train");
        modeChoice.add("Bus");
        modeChoice.add("Ship");
        modeChoice.setBounds(230, 260, 250, 25);
        add(modeChoice);

        // Buttons
        bookBtn = new Button("Book Ticket");
        bookBtn.setBounds(100, 310, 100, 30);
        bookBtn.setBackground(Color.green);
        add(bookBtn);

        resetBtn = new Button("Reset");
        resetBtn.setBounds(220, 310, 100, 30);
        resetBtn.setBackground(Color.yellow);
        add(resetBtn);

        showBtn = new Button("Show All Bookings");
        showBtn.setBounds(340, 310, 130, 30);
        showBtn.setBackground(Color.cyan);
        add(showBtn);

        exitBtn = new Button("Exit");
        exitBtn.setBounds(490, 310, 100, 30);
        exitBtn.setBackground(Color.red);
        add(exitBtn);

        resultLabel = new Label("");
        resultLabel.setBounds(100, 350, 500, 25);
        resultLabel.setForeground(Color.blue);
        add(resultLabel);

        // TextArea to show bookings
        displayArea = new TextArea();
        displayArea.setBounds(50, 390, 600, 150);
        displayArea.setEditable(false);
        add(displayArea);

        // Listeners
        bookBtn.addActionListener(this);
        resetBtn.addActionListener(this);
        showBtn.addActionListener(this);
        exitBtn.addActionListener(this);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                dispose();
                closeConnection();
            }
        });

        connectDatabase();
        loadBookings();
    }

    // Connect to MySQL database
    void connectDatabase() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/travel_agency", "root", "root");
            stmt = conn.createStatement();
            System.out.println("✅ Database Connected Successfully!");
        } catch (Exception e) {
            System.out.println("❌ Database Connection Failed: " + e.getMessage());
        }
    }

    // Close connection
    void closeConnection() {
        try {
            if (stmt != null) stmt.close();
            if (pstmt != null) pstmt.close();
            if (conn != null) conn.close();
            System.out.println("🔒 Connection Closed.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ✅ Load all bookings into text area (well-aligned)
    void loadBookings() {
        try {
            ResultSet rs = stmt.executeQuery("SELECT * FROM bookings");
            StringBuilder sb = new StringBuilder();

            // Header
            sb.append(String.format("%-5s %-20s %-15s %-15s %-10s %-10s%n",
                    "ID", "Customer", "Destination", "Date", "Price", "Mode"));
            sb.append("--------------------------------------------------------------------------\n");

            // Data rows
            while (rs.next()) {
                sb.append(String.format("%-5d %-20s %-15s %-15s %-10.2f %-10s%n",
                        rs.getInt("id"),
                        rs.getString("customer_name"),
                        rs.getString("destination"),
                        rs.getString("travel_date"),
                        rs.getDouble("price"),
                        rs.getString("mode")));
            }

            displayArea.setFont(new Font("Consolas", Font.PLAIN, 14)); // fixed-width font
            displayArea.setText(sb.toString());
        } catch (SQLException e) {
            displayArea.setText("Error loading records: " + e.getMessage());
        }
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == bookBtn) {
            String name = nameField.getText();
            String dest = destField.getText();
            String date = dateField.getText();
            String price = priceField.getText();
            String mode = modeChoice.getSelectedItem();

            if (name.isEmpty() || dest.isEmpty() || date.isEmpty() || price.isEmpty()) {
                resultLabel.setText("⚠️ Please fill all fields!");
            } else {
                try {
                    pstmt = conn.prepareStatement(
                            "INSERT INTO bookings (customer_name, destination, travel_date, price, mode) VALUES (?, ?, ?, ?, ?)");
                    pstmt.setString(1, name);
                    pstmt.setString(2, dest);
                    pstmt.setString(3, date);
                    pstmt.setDouble(4, Double.parseDouble(price));
                    pstmt.setString(5, mode);
                    int rows = pstmt.executeUpdate();

                    if (rows > 0) {
                        resultLabel.setText("✅ Booking saved for " + name + " to " + dest + " (" + mode + ")");
                        loadBookings(); // Refresh table view
                    }
                } catch (Exception ex) {
                    resultLabel.setText("❌ Error saving booking!");
                    ex.printStackTrace();
                }
            }
        }

        if (e.getSource() == resetBtn) {
            nameField.setText("");
            destField.setText("");
            dateField.setText("");
            priceField.setText("");
            modeChoice.select(0);
            resultLabel.setText("");
        }

        if (e.getSource() == showBtn) {
            loadBookings();
        }

        if (e.getSource() == exitBtn) {
            closeConnection();
            System.exit(0);
        }
    }

    public static void main(String[] args) {
        new TravelAgencySystem();
    }
}
