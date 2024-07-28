package application;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.sql.*;
import java.time.LocalDate;

public class ManagePrescription {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/userdb";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "1708";

    public void displayPrescriptionForm() {
        Stage stage = new Stage();
        stage.setTitle("Manage Prescriptions");

        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));

        TextField patientUsernameInput = new TextField();
        patientUsernameInput.setPromptText("Patient Username");

        TextField prescriptionNameInput = new TextField();
        prescriptionNameInput.setPromptText("Prescription Name");

        DatePicker prescriptionDateInput = new DatePicker();
        TextArea prescriptionDetailsInput = new TextArea();
        prescriptionDetailsInput.setPromptText("Prescription Details");

        Button submitButton = new Button("Submit");
        submitButton.setOnAction(e -> {
            String patientUsername = patientUsernameInput.getText();
            String prescriptionName = prescriptionNameInput.getText();
            LocalDate prescriptionDate = prescriptionDateInput.getValue();
            String prescriptionDetails = prescriptionDetailsInput.getText();

            if (patientUsername.isEmpty() || prescriptionName.isEmpty() || prescriptionDate == null || prescriptionDetails.isEmpty()) {
                showAlert("Error", "Please fill all fields.");
                return;
            }

            savePrescriptionToDatabase(patientUsername, prescriptionName, prescriptionDate, prescriptionDetails);
            stage.close();
        });

        layout.getChildren().addAll(new Label("Patient Username"), patientUsernameInput,
                new Label("Prescription Name"), prescriptionNameInput,
                new Label("Prescription Date"), prescriptionDateInput,
                new Label("Prescription Details"), prescriptionDetailsInput,
                submitButton);

        Scene scene = new Scene(layout, 400, 400);
        stage.setScene(scene);
        stage.show();
    }

    private void savePrescriptionToDatabase(String patientUsername, String prescriptionName, LocalDate prescriptionDate, String prescriptionDetails) {
        String sql = "INSERT INTO prescriptions (patient_username, prescription_name, prescription_date, prescription_details) VALUES (?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, patientUsername);
            pstmt.setString(2, prescriptionName);
            pstmt.setDate(3, Date.valueOf(prescriptionDate));
            pstmt.setString(4, prescriptionDetails);
            pstmt.executeUpdate();
            showAlert("Success", "Prescription saved successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Failed to save prescription.");
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
