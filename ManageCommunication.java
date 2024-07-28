package application;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.sql.*;

public class ManageCommunication {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/userdb"; // Update with your database name
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "1708";

    private ListView<String> messagesList;
    private TextArea responseTextArea;

    public void showManageCommunication(Stage primaryStage) {
        primaryStage.setTitle("Manage Communication");

        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));

        messagesList = new ListView<>();
        loadAllMessagesFromDatabase();

        responseTextArea = new TextArea();
        responseTextArea.setPromptText("Enter your response here...");

        Button sendResponseButton = new Button("Send Response");
        sendResponseButton.setOnAction(e -> sendResponse());

        layout.getChildren().addAll(new Label("Messages:"), messagesList, new Label("Response:"), responseTextArea, sendResponseButton);

        Scene scene = new Scene(layout, 400, 400);
        primaryStage.setScene(scene);
        primaryStage.show();
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
                messagesList.getItems().remove(selectedMessage);
                loadAllMessagesFromDatabase();
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

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
