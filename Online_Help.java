package application;

import javafx.scene.control.Alert;
import javafx.scene.control.TextInputDialog;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Online_Help {

    private static final Logger LOGGER = Logger.getLogger(Online_Help.class.getName());

    public void displayHelpDialog() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Online Help");
        dialog.setHeaderText(null);
        dialog.setContentText("Enter your query:");

        dialog.showAndWait().ifPresent(this::searchQuery);
    }

    private void searchQuery(String query) {
        try {
            String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
            String urlString = String.format("https://www.google.com/search?q=%s", encodedQuery);

            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(new URI(urlString));
            } else {
                displayError("Desktop is not supported. Unable to open the web browser.");
            }
        } catch (IOException | URISyntaxException e) {
            LOGGER.log(Level.SEVERE, "Exception occurred:", e);
            displayError("Failed to open the web browser.");
        }
    }

    private void displayError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText(null);
        alert.setTitle("Error");
        alert.setContentText(message);
        alert.showAndWait();
    }
}
