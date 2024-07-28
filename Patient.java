package application;

import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Patient {
    private VBox patientDashboard;
    private Online_Help onlineHelp;
    private generate_invoice invoice;
    private feedback feedback;
    private PrescriptionManagement prescriptionManagement;
    private ManageReport manageReport;

    private ListView<String> availableAppointmentsList;
    private ListView<String> bookedAppointmentsList;
    private ListView<String> invoiceList;
    private ListView<String> prescriptionList;
    private ListView<String> reportList;
    private static final String DB_URL = "jdbc:mysql://localhost:3306/userdb"; // Update with your database name
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "1708";

    private Communication communication;
    private String username; // Patient's username

    public Patient(Stage primaryStage, Scene mainScene, String username) {
        this.communication = new Communication(username);
        this.onlineHelp = new Online_Help();
        this.invoice = new generate_invoice();
        this.feedback = new feedback();
        this.prescriptionManagement = new PrescriptionManagement();
        this.manageReport = new ManageReport();

        this.username = username;
    }

    public VBox getPatientDashboard() {
        if (patientDashboard == null) {
            initializeDashboard();
        }
        return patientDashboard;
    }

    private void initializeDashboard() {
        patientDashboard = new VBox(10);
        patientDashboard.setPadding(new Insets(20, 20, 20, 20));

        String imagePath = "file:/C:/Users/bilal/OneDrive/Desktop/esclips/SDA_FINAL/back.jpg";
        Image image = new Image(imagePath);
        if (image.isError()) {
            System.out.println("Failed to load background image.");
            image.getException().printStackTrace();
        } else {
            BackgroundImage backgroundImage = new BackgroundImage(image,
                    BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER,
                    new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, true, true, true, false));
            patientDashboard.setBackground(new Background(backgroundImage));
        }

        Label welcomeLabel = new Label("Patient Dashboard");
        welcomeLabel.styleProperty().bind(
                Bindings.concat("-fx-font-size: ", patientDashboard.widthProperty().divide(35).asString(), "; ",
                        "-fx-font-weight: bold;")
        );

        Button appointmentButton = new Button("Book Appointment");
        appointmentButton.setOnAction(event -> openBookAppointment());

        Button manageAppointmentsButton = new Button("Manage Appointments");
        manageAppointmentsButton.setOnAction(event -> manageBookedAppointments());

        Button communicationButton = new Button("Communication Platform");
        communicationButton.setOnAction(event -> communication.openCommunicationPlatform((Stage) patientDashboard.getScene().getWindow()));

        Button helpButton = new Button("Online Help");
        helpButton.setOnAction(event -> onlineHelp.displayHelpDialog());

        Button feedbackButton = new Button("Rating and Feedback");
        feedbackButton.setOnAction(event -> feedback.displayFeedbackDialog());

        Button invoiceButton = new Button("View Invoices");
        invoiceButton.setOnAction(event -> showInvoices());

        Button reportsButton = new Button("See Medical Reports");
        reportsButton.setOnAction(event -> showReports());

        Button prescriptionButton = new Button("See Doctor Prescription and Tests");
        prescriptionButton.setOnAction(event -> showPrescriptions());

        patientDashboard.getChildren().addAll(welcomeLabel, appointmentButton, manageAppointmentsButton, communicationButton, helpButton,
                feedbackButton, invoiceButton, reportsButton, prescriptionButton);
    }

    private void openBookAppointment() {
        Stage stage = new Stage();
        stage.setTitle("Book Appointment");

        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));

        availableAppointmentsList = new ListView<>();
        loadAvailableAppointmentsFromDatabase();

        Button bookButton = new Button("Book Selected Appointment");
        bookButton.setOnAction(e -> {
            String selected = availableAppointmentsList.getSelectionModel().getSelectedItem();
            if (selected != null) {
                bookAppointmentInDatabase(selected);
                stage.close();
            } else {
                showAlert("Error", "Please select an appointment to book.");
            }
        });

        layout.getChildren().addAll(new Label("Available Appointments:"), availableAppointmentsList, bookButton);

        Scene scene = new Scene(layout);
        stage.setScene(scene);
        stage.show();
    }

    private void loadAvailableAppointmentsFromDatabase() {
        String sql = "SELECT * FROM available_appointments";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String doctor = rs.getString("doctor");
                LocalDate date = rs.getDate("date").toLocalDate();
                String time = rs.getString("time");
                availableAppointmentsList.getItems().add(doctor + " - " + date.format(DateTimeFormatter.ofPattern("MMMM d, yyyy")) + " at " + time);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Failed to load available appointments: " + e.getMessage());
        }
    }

    private void bookAppointmentInDatabase(String appointment) {
        String[] parts = appointment.split(" - ");
        String doctor = parts[0];
        String[] dateAndTime = parts[1].split(" at ");
        LocalDate date = LocalDate.parse(dateAndTime[0], DateTimeFormatter.ofPattern("MMMM d, yyyy"));
        String time = dateAndTime[1];

        String sql = "INSERT INTO booked_appointments (patient_username, doctor, date, time) VALUES (?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, doctor);
            pstmt.setDate(3, Date.valueOf(date));
            pstmt.setString(4, time);
            pstmt.executeUpdate();
            showAlert("Success", "Appointment booked successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Failed to book appointment: " + e.getMessage());
        }
    }

    private void manageBookedAppointments() {
        Stage stage = new Stage();
        stage.setTitle("Manage Booked Appointments");

        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));

        bookedAppointmentsList = new ListView<>();
        loadBookedAppointmentsFromDatabase();

        Button deleteButton = new Button("Delete Selected");
        deleteButton.setOnAction(e -> {
            String selected = bookedAppointmentsList.getSelectionModel().getSelectedItem();
            if (selected != null) {
                deleteBookedAppointmentFromDatabase(selected);
            } else {
                showAlert("Error", "Please select an appointment to delete.");
            }
        });

        layout.getChildren().addAll(new Label("Booked Appointments:"), bookedAppointmentsList, deleteButton);

        Scene scene = new Scene(layout);
        stage.setScene(scene);
        stage.show();
    }

    private void loadBookedAppointmentsFromDatabase() {
        String sql = "SELECT * FROM booked_appointments WHERE patient_username = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String doctor = rs.getString("doctor");
                LocalDate date = rs.getDate("date").toLocalDate();
                String time = rs.getString("time");
                bookedAppointmentsList.getItems().add(doctor + " - " + date.format(DateTimeFormatter.ofPattern("MMMM d, yyyy")) + " at " + time);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Failed to load booked appointments: " + e.getMessage());
        }
    }

    private void deleteBookedAppointmentFromDatabase(String appointment) {
        String[] parts = appointment.split(" - ");
        String doctor = parts[0];
        String[] dateAndTime = parts[1].split(" at ");
        LocalDate date = LocalDate.parse(dateAndTime[0], DateTimeFormatter.ofPattern("MMMM d, yyyy"));
        String time = dateAndTime[1];

        String sql = "DELETE FROM booked_appointments WHERE patient_username = ? AND doctor = ? AND date = ? AND time = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, doctor);
            pstmt.setDate(3, Date.valueOf(date));
            pstmt.setString(4, time);
            pstmt.executeUpdate();
            bookedAppointmentsList.getItems().remove(appointment);
            showAlert("Success", "Booked appointment deleted successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Failed to delete booked appointment: " + e.getMessage());
        }
    }

    private void showInvoices() {
        ManageInvoice manageInvoice = new ManageInvoice();
        manageInvoice.showInvoicesForPatient(username);
    }

    private void showPrescriptions() {
        Stage stage = new Stage();
        stage.setTitle("Patient Prescriptions");

        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));

        prescriptionList = new ListView<>();
        loadPrescriptionsFromDatabase();

        Button viewButton = new Button("View Selected Prescription");
        viewButton.setOnAction(e -> {
            String selected = prescriptionList.getSelectionModel().getSelectedItem();
            if (selected != null) {
                viewPrescription(selected);
            } else {
                showAlert("Error", "Please select a prescription to view.");
            }
        });

        Button downloadButton = new Button("Download Selected Prescription");
        downloadButton.setOnAction(e -> {
            String selected = prescriptionList.getSelectionModel().getSelectedItem();
            if (selected != null) {
                downloadPrescription(selected);
            } else {
                showAlert("Error", "Please select a prescription to download.");
            }
        });

        layout.getChildren().addAll(new Label("Prescriptions:"), prescriptionList, viewButton, downloadButton);

        Scene scene = new Scene(layout);
        stage.setScene(scene);
        stage.show();
    }

    private void loadPrescriptionsFromDatabase() {
        String sql = "SELECT * FROM prescriptions WHERE patient_username = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String prescriptionName = rs.getString("prescription_name");
                String prescriptionDetails = rs.getString("prescription_details");
                Date date = rs.getDate("prescription_date");
                prescriptionList.getItems().add(prescriptionName + " - " + prescriptionDetails + " - " + date);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Failed to load prescriptions: " + e.getMessage());
        }
    }

    private void viewPrescription(String prescription) {
        showAlert("Prescription Details", prescription);
    }

    private void downloadPrescription(String prescription) {
        String[] parts = prescription.split(" - ");
        String prescriptionName = parts[0];
        String prescriptionDetails = parts[1];
        Date date = Date.valueOf(parts[2]);

        // Save prescription to a file
        try {
            FileWriter writer = new FileWriter("prescription_" + prescriptionName + ".txt");
            writer.write("Prescription: " + prescriptionName + "\n");
            writer.write("Details: " + prescriptionDetails + "\n");
            writer.write("Date: " + date + "\n");
            writer.close();

            showAlert("Download Prescription", "Prescription has been downloaded successfully.");
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to download prescription: " + e.getMessage());
        }
    }

    private void showReports() {
        Stage stage = new Stage();
        stage.setTitle("Patient Reports");

        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));

        reportList = new ListView<>();
        loadReportsFromDatabase();

        Button viewButton = new Button("View Selected Report");
        viewButton.setOnAction(e -> {
            String selected = reportList.getSelectionModel().getSelectedItem();
            if (selected != null) {
                viewReport(selected);
            } else {
                showAlert("Error", "Please select a report to view.");
            }
        });

        Button downloadButton = new Button("Download Selected Report");
        downloadButton.setOnAction(e -> {
            String selected = reportList.getSelectionModel().getSelectedItem();
            if (selected != null) {
                downloadReport(selected);
            } else {
                showAlert("Error", "Please select a report to download.");
            }
        });

        layout.getChildren().addAll(new Label("Reports:"), reportList, viewButton, downloadButton);

        Scene scene = new Scene(layout);
        stage.setScene(scene);
        stage.show();
    }

    private void loadReportsFromDatabase() {
        String sql = "SELECT * FROM reports WHERE patient_username = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String reportName = rs.getString("report_name");
                String reportDetails = rs.getString("report_details");
                Date date = rs.getDate("date");
                reportList.getItems().add(reportName + " - " + reportDetails + " - " + date);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Failed to load reports: " + e.getMessage());
        }
    }

    private void viewReport(String report) {
        showAlert("Report Details", report);
    }

    private void downloadReport(String report) {
        String[] parts = report.split(" - ");
        String reportName = parts[0];
        String reportDetails = parts[1];
        Date date = Date.valueOf(parts[2]);

        // Save report to a file
        try {
            FileWriter writer = new FileWriter("report_" + reportName + ".txt");
            writer.write("Report: " + reportName + "\n");
            writer.write("Details: " + reportDetails + "\n");
            writer.write("Date: " + date + "\n");
            writer.close();

            showAlert("Download Report", "Report has been downloaded successfully.");
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to download report: " + e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public PrescriptionManagement getPrescriptionManagement() {
        return prescriptionManagement;
    }

    public void setPrescriptionManagement(PrescriptionManagement prescriptionManagement) {
        this.prescriptionManagement = prescriptionManagement;
    }

    public feedback getFeedback() {
        return feedback;
    }

    public void setFeedback(feedback feedback) {
        this.feedback = feedback;
    }

    public generate_invoice getInvoice() {
        return invoice;
    }

    public void setInvoice(generate_invoice invoice) {
        this.invoice = invoice;
    }
}
