package com.rct.restclienttool;

import com.rct.restclienttool.model.HttpRequest;
import com.rct.restclienttool.util.CurlImporter;
import com.rct.restclienttool.util.ValidationUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DialogPane;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import org.kordamp.bootstrapfx.BootstrapFX;

import java.util.List;

/**
* Controller for the curl import dialog
*/
public class CurlImportDialogController {
    @FXML private TextArea curlCommandArea;
    @FXML private Button cancelButton;
    @FXML private Button importButton;
   
    private HttpRequest importedRequest;
    private boolean importSuccessful = false;
   
    @FXML
    private void initialize() {
        // Enable import button only when text is entered
        importButton.disableProperty().bind(curlCommandArea.textProperty().isEmpty());
    }
   
    @FXML
    private void handleCancel() {
        closeDialog();
    }
   
    @FXML
    private void handleImport() {
        String curlCommand = curlCommandArea.getText().trim();
       
        // Validate that it's a curl command
        if (!CurlImporter.isValidCurlCommand(curlCommand)) {
            showAlert(Alert.AlertType.ERROR, "Invalid Command",
                "The text entered is not a valid curl command. It should start with 'curl'.");
            return;
        }
       
        try {
            // Parse the curl command
            importedRequest = CurlImporter.parseCurlCommand(curlCommand);
           
            // Validate the parsed request
            List<ValidationUtils.ValidationResult> validationResults = ValidationUtils.validateRequest(importedRequest);
            boolean hasErrors = false;
            StringBuilder errorMessage = new StringBuilder("The curl command has validation issues:\n");
           
            for (ValidationUtils.ValidationResult result : validationResults) {
                if (!result.isValid()) {
                    hasErrors = true;
                    errorMessage.append("• ").append(result.getMessage()).append("\n");
                }
            }
           
            if (hasErrors) {
                // Show warning but still allow import
                showAlert(Alert.AlertType.WARNING, "Import Warning",
                    errorMessage.toString() + "\n\nThe command was parsed but may have issues.");
            }
           
            importSuccessful = true;
            closeDialog();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Import Error",
                "Failed to parse the curl command: " + e.getMessage());
        }
    }
   
    private void closeDialog() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
   
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
       
        // Apply Bootstrap styling
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(BootstrapFX.bootstrapFXStylesheet());
       
        // Add appropriate style class based on alert type
        switch (alertType) {
            case ERROR:
                dialogPane.getStyleClass().addAll("alert", "alert-danger");
                break;
            case WARNING:
                dialogPane.getStyleClass().addAll("alert", "alert-warning");
                break;
            case INFORMATION:
                dialogPane.getStyleClass().addAll("alert", "alert-info");
                break;
            case CONFIRMATION:
                dialogPane.getStyleClass().addAll("alert", "alert-primary");
                break;
        }
       
        alert.showAndWait();
    }
   
    /**
     * Get the imported request
     * @return The HttpRequest created from the curl command
     */
    public HttpRequest getImportedRequest() {
        return importedRequest;
    }
   
    /**
     * Check if the import was successful
     * @return true if the import was successful
     */
    public boolean isImportSuccessful() {
        return importSuccessful;
    }
}