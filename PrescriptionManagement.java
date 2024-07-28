package application;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class PrescriptionManagement {
    private static final Map<String, String> PRESCRIPTIONS_MAP = new HashMap<>();

    static {
        PRESCRIPTIONS_MAP.put("Medicine A", "10mg, Instructions: Take one pill daily");
        PRESCRIPTIONS_MAP.put("Medicine B", "5mg, Instructions: Take two pills daily");
        PRESCRIPTIONS_MAP.put("Blood Test", "Date: 2023-05-01");
        PRESCRIPTIONS_MAP.put("X-Ray", "Date: 2023-04-20");
    }

    public static Map<String, String> getPrescriptionsMap() {
        return PRESCRIPTIONS_MAP;
    }

    public void generatePrescription(String patientUsername, Map<String, Integer> selectedPrescriptions) {
        String sql = "INSERT INTO prescriptions (patient_username, prescription_name, prescription_date, prescription_details) VALUES (?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/userdb", "root", "1708");
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (Map.Entry<String, Integer> entry : selectedPrescriptions.entrySet()) {
                pstmt.setString(1, patientUsername);
                pstmt.setString(2, entry.getKey());
                pstmt.setDate(3, Date.valueOf(LocalDate.now())); // Assuming current date for prescription_date
                pstmt.setString(4, PRESCRIPTIONS_MAP.get(entry.getKey()));
                pstmt.addBatch();
            }
            pstmt.executeBatch();
            System.out.println("Prescriptions saved successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Failed to save prescriptions: " + e.getMessage());
        }
    }
}
