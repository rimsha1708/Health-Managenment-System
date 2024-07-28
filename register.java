package application;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.animation.FadeTransition;
import javafx.util.Duration;
import java.time.LocalDate;
import java.sql.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class register {

    private BorderPane root;
    private GridPane loginPane;
    private VBox signupPane;
    private Patient patient;
    private HealthcareProvider healthcareProvider;

    private static final String DB_URL = "jdbc:mysql://localhost:3306/userdb"; // Update with your database name
    private static final String DB_USER = "root"; // Update with your database username
    private static final String DB_PASSWORD = "1708"; // Update with your database password

    public register(BorderPane root) {
        this.root = root;
    }

    public void showLoginForm() {
        loginPane = new GridPane();
        loginPane.setPadding(new Insets(20, 20, 20, 20));
        loginPane.setVgap(10);
        loginPane.setHgap(10);
        loginPane.setStyle("-fx-background-color: rgba(255, 255, 255, 0.7); -fx-background-radius: 10px;");

        Label usernameLabel = new Label("Username:");
        GridPane.setConstraints(usernameLabel, 0, 0);

        TextField usernameInput = new TextField();
        GridPane.setConstraints(usernameInput, 1, 0);

        Label passwordLabel = new Label("Password:");
        GridPane.setConstraints(passwordLabel, 0, 1);

        PasswordField passwordInput = new PasswordField();
        GridPane.setConstraints(passwordInput, 1, 1);

        Button loginButton = new Button("Login");
        loginButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        GridPane.setConstraints(loginButton, 1, 2);
        loginButton.setOnAction(e -> loginUser(usernameInput.getText(), passwordInput.getText()));

        loginPane.getChildren().addAll(usernameLabel, usernameInput, passwordLabel, passwordInput, loginButton);

        root.setCenter(loginPane);

        FadeTransition fadeIn = new FadeTransition(Duration.seconds(0.5), loginPane);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();
    }

    private void loginUser(String username, String password) {
        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Error", "Please fill in all the fields.");
            return;
        }

        String role = getUserRole(username, password);
        if (role != null) {
            switch (role) {
                case "Patient":
                    patient = new Patient(new Stage(), new Scene(new VBox(), 400, 400), username);
                    root.setCenter(patient.getPatientDashboard());
                    break;
                case "Health Care Provider":
                    healthcareProvider = new HealthcareProvider(new Stage(), new Scene(new VBox(), 400, 400));
                    root.setCenter(healthcareProvider.getHealthcareProviderDashboard());
                    break;
                default:
                    showAlert("Error", "Invalid role. Access denied.");
                    break;
            }
        } else {
            showAlert("Error", "Invalid username or password.");
        }
    }

    public void showSignupForm() {
        signupPane = new VBox(10);
        signupPane.setPadding(new Insets(20, 20, 20, 20));
        signupPane.setStyle("-fx-background-color: rgba(255, 255, 255, 0.7); -fx-background-radius: 10px;");

        TextField usernameInput = new TextField();
        usernameInput.setPromptText("Username");

        PasswordField passwordInput = new PasswordField();
        passwordInput.setPromptText("Password");

        PasswordField confirmPasswordInput = new PasswordField();
        confirmPasswordInput.setPromptText("Confirm Password");

        DatePicker dobPicker = new DatePicker();
        dobPicker.setPromptText("Date of Birth");

        TextField emailInput = new TextField();
        emailInput.setPromptText("Email");

        ComboBox<String> categoryCombo = new ComboBox<>();
        categoryCombo.getItems().addAll("Patient", "Health Care Provider");
        categoryCombo.setPromptText("Select Category");

        Button signupButton = new Button("Signup");
        signupButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
        signupButton.setOnAction(e -> registerUser(usernameInput.getText(), passwordInput.getText(), confirmPasswordInput.getText(), dobPicker.getValue(), emailInput.getText(), categoryCombo.getValue()));

        signupPane.getChildren().addAll(usernameInput, passwordInput, confirmPasswordInput, dobPicker, emailInput, categoryCombo, signupButton);

        root.setCenter(signupPane);

        FadeTransition fadeIn = new FadeTransition(Duration.seconds(0.5), signupPane);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();
    }

    private void registerUser(String username, String password, String confirmPassword, LocalDate dob, String email, String category) {
        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || dob == null || email.isEmpty() || category == null) {
            showAlert("Error", "Please fill in all fields.");
            return;
        }

        if (!email.contains("@")) {
            showAlert("Error", "Invalid email address.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showAlert("Error", "Passwords do not match.");
            return;
        }

        boolean success = registerUserInDatabase(username, password, dob, email, category);
        if (success) {
            showAlert("Success", "User registered successfully as " + category);
            showLoginForm();
        } else {
            showAlert("Error", "Registration failed. Please try again.");
        }
    }

    private boolean registerUserInDatabase(String username, String password, LocalDate dob, String email, String category) {
        String hashedPassword = hashPassword(password);
        String sql = "INSERT INTO users (username, password, dob, email, category) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, hashedPassword);
            pstmt.setDate(3, Date.valueOf(dob));
            pstmt.setString(4, email);
            pstmt.setString(5, category);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            logError("Database Error", e.getMessage());
            return false;
        }
    }

    private String getUserRole(String username, String password) {
        String hashedPassword = hashPassword(password);
        String sql = "SELECT category FROM users WHERE username = ? AND password = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, hashedPassword);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("category");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            logError("Database Error", e.getMessage());
        }
        return null;
    }

    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void logError(String title, String message) {
        // Log the error to a file or monitoring system
        System.err.println(title + ": " + message);
    }
}
