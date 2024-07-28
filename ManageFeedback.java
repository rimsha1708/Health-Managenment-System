package application;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class ManageFeedback {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/userdb"; // Update with your database name
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "1708";

    public void displayFeedbackManagement() {
        Stage stage = new Stage();
        stage.setTitle("Manage Feedback");

        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));

        ListView<String> feedbackList = new ListView<>();
        loadFeedbackFromDatabase(feedbackList);

        layout.getChildren().addAll(new Label("Patient Feedback:"), feedbackList);

        Scene scene = new Scene(layout);
        stage.setScene(scene);
        stage.show();
    }

    private void loadFeedbackFromDatabase(ListView<String> feedbackList) {
        String sql = "SELECT doctor_name, text_content, star_rating FROM feedback";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String doctorName = rs.getString("doctor_name");
                String textContent = rs.getString("text_content");
                int starRating = rs.getInt("star_rating");
                feedbackList.getItems().add("Doctor: " + doctorName + ", Rating: " + starRating + "\nFeedback: " + textContent);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Failed to load feedback from database.");
        }
    }
}
