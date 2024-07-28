package application;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class Main extends Application {

    private BorderPane root;
    private register register;
    private Scene mainScene;
    private Patient patient;
    private HealthcareProvider healthcareProvider;
    private Scene introScene;

    @Override
    public void start(Stage primaryStage) {
        try {
            root = new BorderPane();

            // Load and set the background image
            String backgroundPath = "file:/C:/Users/bilal/OneDrive/Desktop/esclips/SDA_FINAL/back.jpg"; // Ensure this is the correct path
            Image backgroundImage = new Image(backgroundPath, true); // Setting preserveRatio to true
            BackgroundSize backgroundSize = new BackgroundSize(100, 100, true, true, true, true);
            BackgroundImage myBI = new BackgroundImage(backgroundImage,
                    BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER,
                    backgroundSize);
            Background background = new Background(myBI);

            root.setBackground(background);

            // Load and set the application icon
            String iconPath = "file:/C:/Users/bilal/OneDrive/Desktop/esclips/SDA_FINAL/download.png"; // Ensure this is the correct path
            Image icon = new Image(iconPath);
            primaryStage.getIcons().add(icon);

            register = new register(root);
            setPatient(new Patient(primaryStage, mainScene, backgroundPath)); // Initialize Patient with primaryStage and mainScene

            Button loginButton = new Button("Login");
            loginButton.setOnAction(e -> register.showLoginForm());
            loginButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");

            Button signupButton = new Button("Signup");
            signupButton.setOnAction(e -> register.showSignupForm());
            signupButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");

            HBox centerButtonBox = new HBox(10, loginButton, signupButton);
            centerButtonBox.setAlignment(Pos.CENTER);

            VBox vbox = new VBox(centerButtonBox);
            vbox.setAlignment(Pos.CENTER);
            vbox.setPrefHeight(400); // Adjust as necessary to match the scene height

            root.setCenter(vbox);

            mainScene = new Scene(root, 400, 400);

            // Intro Scene
            Label titleLabel = new Label("HEALTH MANAGEMENT SYSTEM");
            titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: black;"); // Set text color to black
            Button nextButton = new Button("Next");
            nextButton.setOnAction(e -> primaryStage.setScene(mainScene));
            nextButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
            VBox introBox = new VBox(20, titleLabel, nextButton);
            introBox.setAlignment(Pos.CENTER);
            introBox.setPadding(new Insets(20));
            introBox.setBackground(background); // Set the same background to the introBox
            introScene = new Scene(introBox, 400, 400);

            primaryStage.setScene(introScene);
            primaryStage.setTitle("Health Management System");
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public HealthcareProvider getHealthcareProvider() {
        return healthcareProvider;
    }

    public void setHealthcareProvider(HealthcareProvider healthcareProvider) {
        this.healthcareProvider = healthcareProvider;
    }
}
