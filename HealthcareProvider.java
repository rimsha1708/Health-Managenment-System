package application;

import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class HealthcareProvider {
    private VBox healthcareProviderDashboard;
    private Online_Help onlineHelp;
    private generate_invoice invoice;
    private feedback feedback;
    private PrescriptionManagement prescriptionManagement;
    private ManageInvoice manageInvoice;
    private ManageReport manageReport;

    private ListView<String> appointmentsList;
    private ListView<String> availableAppointmentsList;
    private static final String DB_URL = "jdbc:mysql://localhost:3306/userdb"; // Update with your database name
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "1708";

    private Communication communication;

    public HealthcareProvider(Stage primaryStage, Scene mainScene) {
        this.communication = new Communication(true);
        this.onlineHelp = new Online_Help();
        this.setInvoice(new generate_invoice());
        this.feedback = new feedback();
        this.prescriptionManagement = new PrescriptionManagement();
        this.manageInvoice = new ManageInvoice();
        this.manageReport = new ManageReport();
    }

    public VBox getHealthcareProviderDashboard() {
        if (healthcareProviderDashboard == null) {
            initializeDashboard();
        }
        return healthcareProviderDashboard;
    }

    private void initializeDashboard() {
        healthcareProviderDashboard = new VBox(10);
        healthcareProviderDashboard.setPadding(new Insets(20, 20, 20, 20));

        String imagePath = "file:/C:/Users/bilal/OneDrive/Desktop/esclips/SDA_FINAL/back.jpg";
        Image image = new Image(imagePath);
        if (image.isError()) {
            System.out.println("Failed to load background image.");
            image.getException().printStackTrace();
        } else {
            BackgroundImage backgroundImage = new BackgroundImage(image,
                BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER,
                new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, true, true, true, false));
            healthcareProviderDashboard.setBackground(new Background(backgroundImage));
        }

        Label welcomeLabel = new Label("Healthcare Provider Dashboard");
        welcomeLabel.styleProperty().bind(
            Bindings.concat("-fx-font-size: ", healthcareProviderDashboard.widthProperty().divide(35).asString(), "; ",
                "-fx-font-weight: bold;")
        );

        Button createAppointmentButton = new Button("Create Available Appointments");
        createAppointmentButton.setOnAction(event -> createAvailableAppointments());

        Button manageAppointmentsButton = new Button("Manage Booked Appointments");
        manageAppointmentsButton.setOnAction(event -> manageBookedAppointments());

        Button viewAvailableAppointmentsButton = new Button("View and Edit Available Appointments");
        viewAvailableAppointmentsButton.setOnAction(event -> viewAndEditAvailableAppointments());

        Button communicationButton = new Button("Communication Platform");
        communicationButton.setOnAction(event -> communication.openCommunicationPlatform((Stage) healthcareProviderDashboard.getScene().getWindow()));

        Button helpButton = new Button("Online Help");
        helpButton.setOnAction(event -> onlineHelp.displayHelpDialog());

        Button feedbackButton = new Button("Manage Feedback");
        feedbackButton.setOnAction(event -> new ManageFeedback().displayFeedbackManagement());

        Button invoiceButton = new Button("Generate Invoice");
        invoiceButton.setOnAction(event -> displayInvoiceForm());

        Button reportsButton = new Button("Manage Reports");
        reportsButton.setOnAction(event -> displayReportForm());

        Button prescriptionButton = new Button("Manage Prescriptions");
        prescriptionButton.setOnAction(event -> displayPrescriptionForm());

        healthcareProviderDashboard.getChildren().addAll(welcomeLabel, createAppointmentButton, manageAppointmentsButton, viewAvailableAppointmentsButton, communicationButton, helpButton,
            feedbackButton, invoiceButton, reportsButton, prescriptionButton);
    }

    private void displayInvoiceForm() {
        Stage stage = new Stage();
        stage.setTitle("Generate Invoice");

        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));

        TextField patientUsernameInput = new TextField();
        patientUsernameInput.setPromptText("Patient Username");

        layout.getChildren().addAll(new Label("Patient Username"), patientUsernameInput);

        Map<String, TextField> serviceQuantityInputs = new HashMap<>();
        for (String serviceName : ManageInvoice.SERVICES_MAP.keySet()) {
            TextField quantityInput = new TextField();
            quantityInput.setPromptText("Enter 'yes' or 'no' for " + serviceName);
            serviceQuantityInputs.put(serviceName, quantityInput);
            layout.getChildren().addAll(new Label(serviceName), quantityInput);
        }

        Button submitButton = new Button("Submit");
        submitButton.setOnAction(e -> {
            String patientUsername = patientUsernameInput.getText();
            if (patientUsername.isEmpty()) {
                showAlert("Error", "Please enter the patient username.");
                return;
            }

            Map<String, Integer> selectedServices = new HashMap<>();
            for (Map.Entry<String, TextField> entry : serviceQuantityInputs.entrySet()) {
                String serviceName = entry.getKey();
                String inputText = entry.getValue().getText();
                if (!inputText.isEmpty() && inputText.equalsIgnoreCase("yes")) {
                    selectedServices.put(serviceName, 1); // Assuming 1 for 'yes' input
                }
            }

            if (selectedServices.isEmpty()) {
                showAlert("Error", "Please select at least one service.");
                return;
            }

            manageInvoice.generateInvoice(patientUsername, selectedServices);
            stage.close();
        });

        layout.getChildren().add(submitButton);

        Scene scene = new Scene(layout, 300, 400);
        stage.setScene(scene);
        stage.show();
    }

    private void createAvailableAppointments() {
        Stage stage = new Stage();
        stage.setTitle("Create Available Appointments");

        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));

        TextField doctorInput = new TextField();
        doctorInput.setPromptText("Doctor Name");

        DatePicker datePicker = new DatePicker();
        ComboBox<String> timeSlotComboBox = new ComboBox<>();
        timeSlotComboBox.getItems().addAll("09:00 AM", "10:00 AM", "11:00 AM", "12:00 PM", "01:00 PM", "02:00 PM", "03:00 PM");

        Button confirmButton = new Button("Confirm Appointment");
        confirmButton.setOnAction(e -> {
            if (!doctorInput.getText().isEmpty() && datePicker.getValue() != null && timeSlotComboBox.getValue() != null) {
                saveAvailableAppointmentToDatabase(doctorInput.getText(), datePicker.getValue(), timeSlotComboBox.getValue());
                stage.close();
            } else {
                showAlert("Error", "Please fill all fields.");
            }
        });

        layout.getChildren().addAll(new Label("Doctor Name:"), doctorInput,
            new Label("Select Date:"), datePicker,
            new Label("Select Time Slot:"), timeSlotComboBox,
            confirmButton);

        Scene scene = new Scene(layout);
        stage.setScene(scene);
        stage.show();
    }

    private void saveAvailableAppointmentToDatabase(String doctor, LocalDate date, String time) {
        String sql = "INSERT INTO available_appointments (doctor, date, time) VALUES (?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, doctor);
            pstmt.setDate(2, Date.valueOf(date));
            pstmt.setString(3, time);
            pstmt.executeUpdate();
            showAlert("Success", "Available appointment created successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Failed to create available appointment.");
        }
    }

    private void manageBookedAppointments() {
        Stage stage = new Stage();
        stage.setTitle("Manage Booked Appointments");

        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));

        appointmentsList = new ListView<>();
        loadBookedAppointmentsFromDatabase();

        Button deleteButton = new Button("Delete Selected");
        deleteButton.setOnAction(e -> {
            String selected = appointmentsList.getSelectionModel().getSelectedItem();
            if (selected != null) {
                deleteBookedAppointmentFromDatabase(selected);
            } else {
                showAlert("Error", "Please select an appointment to delete.");
            }
        });

        layout.getChildren().addAll(new Label("Booked Appointments:"), appointmentsList, deleteButton);

        Scene scene = new Scene(layout);
        stage.setScene(scene);
        stage.show();
    }

    private void loadBookedAppointmentsFromDatabase() {
        String sql = "SELECT * FROM booked_appointments";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String patientUsername = rs.getString("patient_username");
                String doctor = rs.getString("doctor");
                LocalDate date = rs.getDate("date").toLocalDate();
                String time = rs.getString("time");
                appointmentsList.getItems().add(patientUsername + " - " + doctor + " - " + date.format(DateTimeFormatter.ofPattern("MMMM d, yyyy")) + " at " + time);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Failed to load booked appointments.");
        }
    }

    private void deleteBookedAppointmentFromDatabase(String appointment) {
        String[] parts = appointment.split(" - ");
        String patientUsername = parts[0];
        String doctor = parts[1];
        String[] dateAndTime = parts[2].split(" at ");
        LocalDate date = LocalDate.parse(dateAndTime[0], DateTimeFormatter.ofPattern("MMMM d, yyyy"));
        String time = dateAndTime[1];

        String sql = "DELETE FROM booked_appointments WHERE patient_username = ? AND doctor = ? AND date = ? AND time = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, patientUsername);
            pstmt.setString(2, doctor);
            pstmt.setDate(3, Date.valueOf(date));
            pstmt.setString(4, time);
            pstmt.executeUpdate();
            appointmentsList.getItems().remove(appointment);
            showAlert("Success", "Booked appointment deleted successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Failed to delete booked appointment.");
        }
    }

    private void viewAndEditAvailableAppointments() {
        Stage stage = new Stage();
        stage.setTitle("View and Edit Available Appointments");

        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));

        availableAppointmentsList = new ListView<>();
        loadAvailableAppointmentsFromDatabase();

        Button editButton = new Button("Edit Selected Appointment");
        editButton.setOnAction(e -> editSelectedAppointment());

        Button deleteButton = new Button("Delete Selected Appointment");
        deleteButton.setOnAction(e -> deleteSelectedAvailableAppointment());

        layout.getChildren().addAll(new Label("Available Appointments:"), availableAppointmentsList, editButton, deleteButton);

        Scene scene = new Scene(layout, 400, 400);
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
            showAlert("Database Error", "Failed to load available appointments.");
        }
    }

    private void editSelectedAppointment() {
        String selectedAppointment = availableAppointmentsList.getSelectionModel().getSelectedItem();
        if (selectedAppointment != null) {
            String[] parts = selectedAppointment.split(" - ");
            String doctor = parts[0];
            String[] dateAndTime = parts[1].split(" at ");
            LocalDate date = LocalDate.parse(dateAndTime[0], DateTimeFormatter.ofPattern("MMMM d, yyyy"));
            String time = dateAndTime[1];

            Stage editStage = new Stage();
            editStage.setTitle("Edit Appointment");

            GridPane editLayout = new GridPane();
            editLayout.setPadding(new Insets(20));
            editLayout.setVgap(10);
            editLayout.setHgap(10);

            TextField doctorInput = new TextField(doctor);
            DatePicker datePicker = new DatePicker(date);
            ComboBox<String> timeSlotComboBox = new ComboBox<>();
            timeSlotComboBox.getItems().addAll("09:00 AM", "10:00 AM", "11:00 AM", "12:00 PM", "01:00 PM", "02:00 PM", "03:00 PM");
            timeSlotComboBox.setValue(time);

            Button saveButton = new Button("Save Changes");
            saveButton.setOnAction(e -> saveAppointmentChanges(selectedAppointment, doctorInput, datePicker, timeSlotComboBox, editStage));

            editLayout.add(new Label("Doctor:"), 0, 0);
            editLayout.add(doctorInput, 1, 0);
            editLayout.add(new Label("Date:"), 0, 1);
            editLayout.add(datePicker, 1, 1);
            editLayout.add(new Label("Time:"), 0, 2);
            editLayout.add(timeSlotComboBox, 1, 2);
            editLayout.add(saveButton, 1, 3);

            Scene editScene = new Scene(editLayout, 300, 200);
            editStage.setScene(editScene);
            editStage.show();
        } else {
            showAlert("No Appointment Selected", "Please select an appointment to edit.");
        }
    }

    private void saveAppointmentChanges(String oldAppointment, TextField doctorInput, DatePicker datePicker, ComboBox<String> timeSlotComboBox, Stage editStage) {
        String newDoctor = doctorInput.getText();
        LocalDate newDate = datePicker.getValue();
        String newTime = timeSlotComboBox.getValue();

        if (newDoctor.isEmpty() || newDate == null || newTime == null) {
            showAlert("Error", "Please fill all fields.");
            return;
        }

        String[] parts = oldAppointment.split(" - ");
        String oldDoctor = parts[0];
        String[] dateAndTime = parts[1].split(" at ");
        LocalDate oldDate = LocalDate.parse(dateAndTime[0], DateTimeFormatter.ofPattern("MMMM d, yyyy"));
        String oldTime = dateAndTime[1];

        String sql = "UPDATE available_appointments SET doctor = ?, date = ?, time = ? WHERE doctor = ? AND date = ? AND time = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newDoctor);
            pstmt.setDate(2, java.sql.Date.valueOf(newDate));
            pstmt.setString(3, newTime);
            pstmt.setString(4, oldDoctor);
            pstmt.setDate(5, java.sql.Date.valueOf(oldDate));
            pstmt.setString(6, oldTime);
            pstmt.executeUpdate();

            availableAppointmentsList.getItems().remove(oldAppointment);
            availableAppointmentsList.getItems().add(newDoctor + " - " + newDate.format(DateTimeFormatter.ofPattern("MMMM d, yyyy")) + " at " + newTime);
            editStage.close();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Failed to update appointment.");
        }
    }

    private void deleteSelectedAvailableAppointment() {
        String selectedAppointment = availableAppointmentsList.getSelectionModel().getSelectedItem();
        if (selectedAppointment != null) {
            String[] parts = selectedAppointment.split(" - ");
            String doctor = parts[0];
            String[] dateAndTime = parts[1].split(" at ");
            LocalDate date = LocalDate.parse(dateAndTime[0], DateTimeFormatter.ofPattern("MMMM d, yyyy"));
            String time = dateAndTime[1];

            String sql = "DELETE FROM available_appointments WHERE doctor = ? AND date = ? AND time = ?";

            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, doctor);
                pstmt.setDate(2, java.sql.Date.valueOf(date));
                pstmt.setString(3, time);
                pstmt.executeUpdate();
                availableAppointmentsList.getItems().remove(selectedAppointment);
                showAlert("Success", "Available appointment deleted successfully.");
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert("Database Error", "Failed to delete available appointment.");
            }
        } else {
            showAlert("No Appointment Selected", "Please select an appointment to delete.");
        }
    }

    private void displayReportForm() {
        Stage stage = new Stage();
        stage.setTitle("Manage Reports");

        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));

        TextField patientUsernameInput = new TextField();
        patientUsernameInput.setPromptText("Patient Username");

        layout.getChildren().addAll(new Label("Patient Username"), patientUsernameInput);

        Map<String, CheckBox> reportOptions = new HashMap<>();
        for (String reportName : ManageReport.getReportsMap().keySet()) {
            CheckBox reportCheckBox = new CheckBox(reportName);
            reportOptions.put(reportName, reportCheckBox);
            layout.getChildren().addAll(new Label(reportName), reportCheckBox);
        }

        Button submitButton = new Button("Submit");
        submitButton.setOnAction(e -> {
            String patientUsername = patientUsernameInput.getText();
            if (patientUsername.isEmpty()) {
                showAlert("Error", "Please enter the patient username.");
                return;
            }

            Map<String, Boolean> selectedReports = new HashMap<>();
            for (Map.Entry<String, CheckBox> entry : reportOptions.entrySet()) {
                String reportName = entry.getKey();
                boolean selected = entry.getValue().isSelected();
                if (selected) {
                    selectedReports.put(reportName, true);
                }
            }

            if (selectedReports.isEmpty()) {
                showAlert("Error", "Please select at least one report.");
                return;
            }

            manageReport.generateReport(patientUsername, selectedReports);
            stage.close();
        });

        layout.getChildren().add(submitButton);

        Scene scene = new Scene(layout, 300, 400);
        stage.setScene(scene);
        stage.show();
    }

    private void displayPrescriptionForm() {
        Stage stage = new Stage();
        stage.setTitle("Manage Prescriptions");

        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));

        TextField patientUsernameInput = new TextField();
        patientUsernameInput.setPromptText("Patient Username");

        layout.getChildren().addAll(new Label("Patient Username"), patientUsernameInput);

        Map<String, TextField> prescriptionInputs = new HashMap<>();
        for (String prescriptionName : PrescriptionManagement.getPrescriptionsMap().keySet()) {
            TextField prescriptionInput = new TextField();
            prescriptionInput.setPromptText("Enter quantity for " + prescriptionName);
            prescriptionInputs.put(prescriptionName, prescriptionInput);
            layout.getChildren().addAll(new Label(prescriptionName), prescriptionInput);
        }

        Button submitButton = new Button("Submit");
        submitButton.setOnAction(e -> {
            String patientUsername = patientUsernameInput.getText();
            if (patientUsername.isEmpty()) {
                showAlert("Error", "Please enter the patient username.");
                return;
            }

            Map<String, Integer> selectedPrescriptions = new HashMap<>();
            for (Map.Entry<String, TextField> entry : prescriptionInputs.entrySet()) {
                String prescriptionName = entry.getKey();
                String inputText = entry.getValue().getText();
                if (!inputText.isEmpty()) {
                    try {
                        selectedPrescriptions.put(prescriptionName, Integer.parseInt(inputText));
                    } catch (NumberFormatException ex) {
                        showAlert("Error", "Please enter a valid quantity for " + prescriptionName + ".");
                        return;
                    }
                }
            }

            if (selectedPrescriptions.isEmpty()) {
                showAlert("Error", "Please select at least one prescription.");
                return;
            }

            prescriptionManagement.generatePrescription(patientUsername, selectedPrescriptions);
            stage.close();
        });

        layout.getChildren().add(submitButton);

        Scene scene = new Scene(layout, 300, 400);
        stage.setScene(scene);
        stage.show();
    }

    private void viewReport(String patientUsername, String reportName) {
        File reportFile = manageReport.getReportFile(patientUsername, reportName);
        if (reportFile != null && reportFile.exists()) {
            try {
                java.awt.Desktop.getDesktop().open(reportFile);
            } catch (IOException e) {
                showAlert("Error", "Failed to open report: " + e.getMessage());
            }
        } else {
            showAlert("Error", "Report file does not exist.");
        }
    }

    private void downloadReport(String patientUsername, String reportName) {
        File reportFile = manageReport.getReportFile(patientUsername, reportName);
        if (reportFile != null && reportFile.exists()) {
            File downloadDir = new File("downloads"); // Update with your download directory path
            if (!downloadDir.exists()) {
                downloadDir.mkdirs();
            }
            File destFile = new File(downloadDir, reportFile.getName());
            try {
                Files.copy(reportFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                showAlert("Success", "Report downloaded successfully.");
            } catch (IOException e) {
                showAlert("Error", "Failed to download report: " + e.getMessage());
            }
        } else {
            showAlert("Error", "Report file does not exist.");
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
