package application;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class ManageInvoice {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/userdb"; // Update with your database name
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "1708";

    static final Map<String, Double> SERVICES_MAP = new HashMap<>();
    static {
        SERVICES_MAP.put("CT Scan", 500.0);
        SERVICES_MAP.put("MRI", 1000.0);
        SERVICES_MAP.put("Blood Test", 100.0);
        SERVICES_MAP.put("X-Ray", 200.0);
        SERVICES_MAP.put("Consultation", 150.0);
    }

    public void generateInvoice(String patientUsername, Map<String, Integer> selectedServices) {
        double totalAmount = 0.0;

        StringBuilder servicesBuilder = new StringBuilder();
        for (Map.Entry<String, Integer> entry : selectedServices.entrySet()) {
            Double servicePrice = SERVICES_MAP.get(entry.getKey());
            if (servicePrice != null) {
                totalAmount += servicePrice * entry.getValue();
                servicesBuilder.append(entry.getKey()).append(" x ").append(entry.getValue()).append(", ");
            } else {
                showAlert("Error", "Service " + entry.getKey() + " is not recognized.");
                return;
            }
        }

        String sqlInvoice = "INSERT INTO invoices (patient_username, total_amount, date, services) VALUES (?, ?, ?, ?)";
        String sqlInvoiceDetails = "INSERT INTO invoice_details (invoice_id, service_name, quantity, price) VALUES (?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            conn.setAutoCommit(false); // Start transaction

            // Insert invoice into invoices table
            int invoiceId;
            try (PreparedStatement pstmt = conn.prepareStatement(sqlInvoice, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, patientUsername);
                pstmt.setDouble(2, totalAmount);
                pstmt.setDate(3, Date.valueOf(LocalDate.now()));
                pstmt.setString(4, servicesBuilder.toString());
                pstmt.executeUpdate();

                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    invoiceId = rs.getInt(1);
                } else {
                    conn.rollback();
                    showAlert("Error", "Failed to generate invoice ID.");
                    return;
                }
            }

            // Insert services into invoice_details table
            try (PreparedStatement pstmt = conn.prepareStatement(sqlInvoiceDetails)) {
                for (Map.Entry<String, Integer> entry : selectedServices.entrySet()) {
                    String serviceName = entry.getKey();
                    int quantity = entry.getValue();
                    double price = SERVICES_MAP.get(serviceName);

                    pstmt.setInt(1, invoiceId);
                    pstmt.setString(2, serviceName);
                    pstmt.setInt(3, quantity);
                    pstmt.setDouble(4, price * quantity);
                    pstmt.addBatch();
                }
                pstmt.executeBatch();
            }

            conn.commit(); // Commit transaction
            showAlert("Success", "Invoice generated successfully.");

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Failed to generate invoice.");
        }
    }

    public void showInvoicesForPatient(String patientUsername) {
        Stage stage = new Stage();
        stage.setTitle("Patient Invoices");

        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));

        ListView<String> invoiceList = new ListView<>();
        loadInvoicesFromDatabase(patientUsername, invoiceList);

        Button downloadButton = new Button("Download Selected Invoice");
        downloadButton.setOnAction(e -> {
            String selected = invoiceList.getSelectionModel().getSelectedItem();
            if (selected != null) {
                downloadInvoice(selected);
            } else {
                showAlert("Error", "Please select an invoice to download.");
            }
        });

        layout.getChildren().addAll(new Label("Invoices:"), invoiceList, downloadButton);

        Scene scene = new Scene(layout);
        stage.setScene(scene);
        stage.show();
    }

    private void loadInvoicesFromDatabase(String patientUsername, ListView<String> invoiceList) {
        String sql = "SELECT * FROM invoices WHERE patient_username = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, patientUsername);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("id");
                Date date = rs.getDate("date");
                invoiceList.getItems().add("Invoice #" + id + " - " + date);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Failed to load invoices.");
        }
    }

    private void downloadInvoice(String invoiceDetails) {
        String[] parts = invoiceDetails.split(" - ");
        int id = Integer.parseInt(parts[0].replaceAll("\\D+", "")); // Extracting numeric ID

        String sql = "SELECT * FROM invoices WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String patientUsername = rs.getString("patient_username");
                Date date = rs.getDate("date");
                String services = rs.getString("services");
                double totalCost = rs.getDouble("total_amount");

                // Save invoice to a file
                FileWriter writer = new FileWriter("invoice_" + id + ".txt");
                writer.write("Invoice #" + id + "\n");
                writer.write("Patient Username: " + patientUsername + "\n");
                writer.write("Date: " + date + "\n");
                writer.write("Services: " + services + "\n");
                writer.write("Total Cost: $" + totalCost + "\n");
                writer.close();

                showAlert("Download Invoice", "Invoice has been downloaded successfully.");
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to download invoice: " + e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
