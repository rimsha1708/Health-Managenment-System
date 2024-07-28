package application;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;

public class ManageReport {

    public static final Map<String, String> REPORTS_MAP = Map.of(
        "Blood Test", "Detailed blood test report",
        "X-Ray", "X-Ray imaging report",
        "MRI", "MRI scan report"
    );

    private static final String DB_URL = "jdbc:mysql://localhost:3306/userdb"; // Update with your database name
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "1708";

    public void generateReport(String patientUsername, Map<String, Boolean> selectedReports) {
        String sql = "INSERT INTO reports (patient_username, report_name, report_details, date) VALUES (?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (Map.Entry<String, Boolean> entry : selectedReports.entrySet()) {
                if (entry.getValue()) {
                    String reportName = entry.getKey();
                    String reportDetails = REPORTS_MAP.get(reportName);
                    pstmt.setString(1, patientUsername);
                    pstmt.setString(2, reportName);
                    pstmt.setString(3, reportDetails);
                    pstmt.setDate(4, new Date(System.currentTimeMillis()));
                    pstmt.executeUpdate();

                    // Generate text file for the report
                    generateReportFile(patientUsername, reportName, reportDetails);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle exceptions
        }
    }

    private void generateReportFile(String patientUsername, String reportName, String reportDetails) {
        String directoryPath = "reports";
        File directory = new File(directoryPath);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        String fileName = directoryPath + "/" + patientUsername + "_" + reportName.replaceAll("\\s+", "_") + ".txt";
        try (FileWriter writer = new FileWriter(fileName)) {
            writer.write("Patient Username: " + patientUsername + "\n");
            writer.write("Report Name: " + reportName + "\n");
            writer.write("Report Details: " + reportDetails + "\n");
            writer.write("Date: " + new Date(System.currentTimeMillis()) + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Map<String, String> getReportsMap() {
        return REPORTS_MAP;
    }

    public File getReportFile(String patientUsername, String reportName) {
        String fileName = "reports/" + patientUsername + "_" + reportName.replaceAll("\\s+", "_") + ".txt";
        File reportFile = new File(fileName);
        if (reportFile.exists()) {
            return reportFile;
        } else {
            return null;
        }
    }
}
