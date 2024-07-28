package application;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.awt.Desktop;
import java.net.URI;
import java.sql.*;

public class Communication extends Application {

    private Scene mainScene; // Store the main scene
    private static final String DB_URL = "jdbc:mysql://localhost:3306/userdb"; // Update with your database name
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "1708";

    private String patientUsername; // Patient's username
    private boolean isHealthcareProvider; // To differentiate between patient and healthcare provider
    private ListView<String> messagesList;
    private TextArea responseTextArea;

    // Constructor for patient
    public Communication(String patientUsername) {
        this.patientUsername = patientUsername;
        this.isHealthcareProvider = false;
    }

    // Constructor for healthcare provider
    public Communication(boolean isHealthcareProvider) {
        this.isHealthcareProvider = isHealthcareProvider;
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Communication Platform");

        if (isHealthcareProvider) {
            showHealthcareProviderInterface(primaryStage);
        } else {
            showPatientInterface(primaryStage);
        }
    }

    private void showPatientInterface(Stage primaryStage) {
        // Create the main page layout for patient
        Button communicationPlatformButton = new Button("Communication Platform");
        communicationPlatformButton.setOnAction(e -> openCommunicationPlatform(primaryStage));

        Button onlineHelpButton = new Button("Online Help");
        onlineHelpButton.setOnAction(e -> {
            Online_Help onlineHelp = new Online_Help();
            onlineHelp.displayHelpDialog();
        });

        VBox mainLayout = new VBox(10);
        mainLayout.setPadding(new Insets(20));
        mainLayout.getChildren().addAll(communicationPlatformButton, onlineHelpButton);

        mainScene = new Scene(mainLayout, 300, 200); // Store the main scene

        // Set the main page scene
        primaryStage.setScene(mainScene);
        primaryStage.show();
    }

    private void showHealthcareProviderInterface(Stage primaryStage) {
        // Create the main page layout for healthcare provider
        Button viewMessagesButton = new Button("View Messages");
        viewMessagesButton.setOnAction(e -> openCommunicationPlatform(primaryStage));

        VBox mainLayout = new VBox(10);
        mainLayout.setPadding(new Insets(20));
        mainLayout.getChildren().addAll(viewMessagesButton);

        mainScene = new Scene(mainLayout, 300, 200); // Store the main scene

        // Set the main page scene
        primaryStage.setScene(mainScene);
        primaryStage.show();
    }

    public void openCommunicationPlatform(Stage primaryStage) {
        primaryStage.setTitle("Communication Platform");

        // Create the menu bar with a back option
        MenuBar menuBar = new MenuBar();
        
        MenuItem backMenuItem = new MenuItem("Back");
        backMenuItem.setOnAction(e -> primaryStage.setScene(mainScene));
        //menuBar.getMenus().add(new Menu("Options", null, backMenuItem));

        // Create communication platform buttons
        Button callButton = new Button("Call");
        callButton.setOnAction(e -> displayCallInformation());

        Button emailButton = new Button("Email");
        emailButton.setOnAction(e -> openEmailClient());

        Button messageButton = new Button("Leave a Message");
        messageButton.setOnAction(e -> leaveMessage());

        Button viewMessagesButton = new Button("View Messages");
        viewMessagesButton.setOnAction(e -> viewMessages());

        // Set background image
        String imagePath = "file:/C:/Users/bilal/OneDrive/Desktop/esclips/SDA_FINAL/back.jpg"; // Ensure this is the correct path
        Image image = new Image(imagePath, true); // Setting preserveRatio to true
        BackgroundSize backgroundSize = new BackgroundSize(100, 100, true, true, true, true);
        BackgroundImage backgroundImage = new BackgroundImage(
                image,
                BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER,
                backgroundSize
        );

        VBox communicationLayout = new VBox(10);
        communicationLayout.setPadding(new Insets(20));

        if (isHealthcareProvider) {
            communicationLayout.getChildren().addAll(menuBar, viewMessagesButton);
        } else {
            communicationLayout.getChildren().addAll(menuBar, callButton, emailButton, messageButton, viewMessagesButton);
        }
        communicationLayout.setBackground(new Background(backgroundImage));

        Scene communicationScene = new Scene(communicationLayout, 300, 200);

        // Set the communication platform scene
        primaryStage.setScene(communicationScene);
        primaryStage.show();
    }

    private void displayCallInformation() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setTitle("Call Healthcare Provider Helpline");
        alert.setContentText("Please call the healthcare provider at: +92-300-555-1234");
        alert.showAndWait();
    }

    private void openEmailClient() {
        try {
            URI mailto = new URI("mailto:healthcare@gmail.com?subject=Healthcare%20Query");
            Desktop.getDesktop().mail(mailto);
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText(null);
            alert.setTitle("Error");
            alert.setContentText("Failed to open the email client.");
            alert.showAndWait();
        }
    }

    private void leaveMessage() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Leave a Message");
        dialog.setHeaderText(null);
        dialog.setContentText("Enter your question:");

        dialog.showAndWait().ifPresent(query -> {
            if (patientUsername != null) {
                saveMessageToDatabase(query);
                showAlert("Message Sent", "Your message has been sent to the healthcare provider.");
            } else {
                showAlert("Error", "Only patients can leave messages.");
            }
        });
    }

    private void viewMessages() {
        Stage stage = new Stage();
        stage.setTitle("Messages");

        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));

        messagesList = new ListView<>();
        if (isHealthcareProvider) {
            loadAllMessagesFromDatabase();
        } else {
            loadMessagesFromDatabase();
        }

        responseTextArea = new TextArea();
        responseTextArea.setPromptText("Enter your response here...");

        Button sendResponseButton = new Button("Send Response");
        sendResponseButton.setOnAction(e -> sendResponse());

        layout.getChildren().addAll(new Label("Messages:"), messagesList, new Label("Response:"), responseTextArea, sendResponseButton);

        Scene scene = new Scene(layout, 400, 400);
        stage.setScene(scene);
        stage.show();
    }

    private void loadMessagesFromDatabase() {
        String sql = "SELECT id, message, response FROM messages WHERE patient_username = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, patientUsername);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("id");
                String message = rs.getString("message");
                String response = rs.getString("response");
                messagesList.getItems().add("ID: " + id + ", Message: " + message + ", Response: " + response);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Failed to load messages.");
        }
    }

    private void loadAllMessagesFromDatabase() {
        String sql = "SELECT id, patient_username, message, response FROM messages";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                int id = rs.getInt("id");
                String patientUsername = rs.getString("patient_username");
                String message = rs.getString("message");
                String response = rs.getString("response");
                messagesList.getItems().add("ID: " + id + ", Patient: " + patientUsername + ", Message: " + message + ", Response: " + response);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Failed to load messages.");
        }
    }

    private void sendResponse() {
        String selectedMessage = messagesList.getSelectionModel().getSelectedItem();
        if (selectedMessage != null) {
            String response = responseTextArea.getText();
            if (!response.isEmpty()) {
                int id = Integer.parseInt(selectedMessage.split(",")[0].split(":")[1].trim());
                updateMessageResponseInDatabase(id, response);
                responseTextArea.clear();
                messagesList.getItems().clear();
                if (isHealthcareProvider) {
                    loadAllMessagesFromDatabase();
                } else {
                    loadMessagesFromDatabase();
                }
            } else {
                showAlert("Error", "Please enter a response.");
            }
        } else {
            showAlert("Error", "Please select a message to respond to.");
        }
    }

    private void updateMessageResponseInDatabase(int id, String response) {
        String sql = "UPDATE messages SET response = ? WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, response);
            pstmt.setInt(2, id);
            pstmt.executeUpdate();
            showAlert("Success", "Response sent successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Failed to send response.");
        }
    }

    private void saveMessageToDatabase(String message) {
        String sql = "INSERT INTO messages (patient_username, message, response) VALUES (?, ?, NULL)";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, patientUsername);
            pstmt.setString(2, message);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Failed to save message.");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
