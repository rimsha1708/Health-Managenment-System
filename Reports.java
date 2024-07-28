package application;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.awt.Desktop;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Reports {
    private VBox reportsDashboard;
    private static final String PDF_FILE_PATH = "dummy_report.pdf";

    public Reports() {
        initializeDashboard();
        createDummyPdfReport();
    }

    public VBox getReportsDashboard() {
        return reportsDashboard;
    }

    private void initializeDashboard() {
        reportsDashboard = new VBox(10);
        reportsDashboard.setPadding(new Insets(20, 20, 20, 20));

        Label titleLabel = new Label("Diagnostic Reports and Imaging");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        ListView<String> reportsListView = new ListView<>();
        reportsListView.getItems().addAll(
                "Blood Test Report - 2023-05-01",
                "X-Ray Imaging - 2023-04-20",
                "MRI Report - 2023-03-15"
        );

        Button viewReportButton = new Button("View Report");
        viewReportButton.setOnAction(e -> viewReport(reportsListView.getSelectionModel().getSelectedItem()));

        Button downloadReportButton = new Button("Download Report");
        downloadReportButton.setOnAction(e -> downloadReport(reportsListView.getSelectionModel().getSelectedItem()));

        reportsDashboard.getChildren().addAll(titleLabel, reportsListView, viewReportButton, downloadReportButton);
    }

    public static void showReportsDialog() {
        Stage stage = new Stage();
        stage.setTitle("Diagnostic Reports and Imaging");

        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));

        Label titleLabel = new Label("Your Diagnostic Reports and Imaging Results");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        ListView<String> reportsListView = new ListView<>();
        reportsListView.getItems().addAll(
                "Blood Test Report - 2023-05-01",
                "X-Ray Imaging - 2023-04-20",
                "MRI Report - 2023-03-15"
        );

        Button viewReportButton = new Button("View Report");
        viewReportButton.setOnAction(e -> viewReport(reportsListView.getSelectionModel().getSelectedItem()));

        Button downloadReportButton = new Button("Download Report");
        downloadReportButton.setOnAction(e -> downloadReport(reportsListView.getSelectionModel().getSelectedItem()));

        layout.getChildren().addAll(titleLabel, reportsListView, viewReportButton, downloadReportButton);

        Scene scene = new Scene(layout, 400, 400);
        stage.setScene(scene);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.showAndWait();
    }

    private static void viewReport(String reportName) {
        if (reportName != null) {
            try {
                File pdfFile = new File(PDF_FILE_PATH);
                if (pdfFile.exists()) {
                    if (Desktop.isDesktopSupported()) {
                        Desktop.getDesktop().open(pdfFile);
                    } else {
                        showAlert("Error", "Desktop is not supported.");
                    }
                } else {
                    showAlert("Error", "PDF file does not exist.");
                }
            } catch (IOException ex) {
                showAlert("Error", "Failed to open the PDF file.");
                ex.printStackTrace();
            }
        } else {
            showAlert("No Report Selected", "Please select a report to view.");
        }
    }

    private static void downloadReport(String reportName) {
        if (reportName != null) {
            try {
                File sourceFile = new File(PDF_FILE_PATH);
                if (sourceFile.exists()) {
                    Path destinationPath = Paths.get(System.getProperty("user.home"), "Downloads", PDF_FILE_PATH);
                    Files.copy(sourceFile.toPath(), destinationPath);
                    showAlert("Download Report", "The report has been downloaded to: " + destinationPath.toString());
                } else {
                    showAlert("Error", "PDF file does not exist.");
                }
            } catch (IOException ex) {
                showAlert("Error", "Failed to download the PDF file.");
                ex.printStackTrace();
            }
        } else {
            showAlert("No Report Selected", "Please select a report to download.");
        }
    }

    private static void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void createDummyPdfReport() {
        try {
            FileWriter writer = new FileWriter(PDF_FILE_PATH);
            writer.write("This is a dummy diagnostic report.\n");
            writer.write("Detailed information about the test results goes here.");
            writer.close();
        } catch (IOException e) {
            showAlert("Error", "Failed to create dummy PDF report.");
            e.printStackTrace();
        }
    }
}