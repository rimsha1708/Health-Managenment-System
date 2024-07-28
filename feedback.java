package application;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class feedback {
    private String textContent;
    private int starRating;

    private static final String DB_URL = "jdbc:mysql://localhost:3306/userdb"; // Update with your database name
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "1708";

    public void displayFeedbackDialog() {
        final Stage stage = new Stage();
        stage.setTitle("Feedback");
        
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));

        TextField doctorInput = new TextField();
        doctorInput.setPromptText("Doctor Name");

        TextArea feedbackTextArea = new TextArea();
        feedbackTextArea.setPromptText("Enter your feedback here...");

        Label ratingLabel = new Label("Rating:");

        Slider ratingSlider = new Slider(0, 5, 0);
        ratingSlider.setShowTickLabels(true);
        ratingSlider.setShowTickMarks(true);
        ratingSlider.setMajorTickUnit(1);
        ratingSlider.setBlockIncrement(1);
        
        Button submitButton = new Button("Submit Feedback");
        submitButton.setOnAction(event -> {
            String doctorName = doctorInput.getText();
            textContent = feedbackTextArea.getText();
            starRating = (int) ratingSlider.getValue();
            saveFeedbackToDatabase(doctorName, textContent, starRating);
            stage.close();
        });

        layout.getChildren().addAll(new Label("Doctor Name:"), doctorInput, feedbackTextArea, ratingLabel, ratingSlider, submitButton);

        Scene scene = new Scene(layout);
        stage.setScene(scene);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.showAndWait();
    }

    private void saveFeedbackToDatabase(String doctorName, String feedback, int rating) {
        String sql = "INSERT INTO feedback (doctor_name, text_content, star_rating) VALUES (?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, doctorName);
            pstmt.setString(2, feedback);
            pstmt.setInt(3, rating);
            pstmt.executeUpdate();
            System.out.println("Feedback saved to database successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Failed to save feedback to database.");
        }
    }
}
