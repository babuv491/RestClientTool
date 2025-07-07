package com.rct.restclienttool;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.DialogPane;
import javafx.scene.image.WritableImage;
import javafx.scene.image.PixelWriter;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.kordamp.bootstrapfx.BootstrapFX;

import java.io.IOException;

/**
* Main JavaFX application class for the REST Client Tool.
* Handles application startup, UI initialization, and styling.
*
 * @author REST Client Tool
* @version 1.0
* @since 1.0
*/
public class RestClientApplication extends Application {
    /**
     * Starts the JavaFX application and initializes the main window.
     *
     * @param stage the primary stage for the application
     * @throws IOException if FXML loading fails
     */
    @Override
    public void start(Stage stage) throws IOException {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(RestClientApplication.class.getResource("rest-client-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 1200, 800);
           
            // Add BootstrapFX and custom stylesheets
            scene.getStylesheets().addAll(
                BootstrapFX.bootstrapFXStylesheet(),
                getClass().getResource("styles.css").toExternalForm()
            );
           
            // Get the controller and set it as the root controller
            RestClientController controller = fxmlLoader.getController();
            controller.setAsRootController();
           
            // Set the controller in the scene's user data for easy access
            scene.setUserData(controller);
           
            stage.setTitle("REST API Client Tool");
           
            // Set minimum window size
            stage.setMinWidth(900);
            stage.setMinHeight(600);
           
            // Create a simple application icon
            try {
                // Create a simple colored rectangle icon
                int size = 16;
                javafx.scene.image.WritableImage icon = new javafx.scene.image.WritableImage(size, size);
                javafx.scene.image.PixelWriter pixelWriter = icon.getPixelWriter();
               
                // Fill with blue color
                javafx.scene.paint.Color iconColor = javafx.scene.paint.Color.web("#2196F3");
                for (int y = 0; y < size; y++) {
                    for (int x = 0; x < size; x++) {
                        // Create rounded corners by skipping corner pixels
                        boolean isCorner = (x < 2 && y < 2) || (x < 2 && y > size-3) ||
                                          (x > size-3 && y < 2) || (x > size-3 && y > size-3);
                        if (!isCorner) {
                            pixelWriter.setColor(x, y, iconColor);
                        }
                    }
                }
               
                // Add the icon to the stage
                stage.getIcons().add(icon);
            } catch (Exception e) {
                // Icon creation is optional
                System.err.println("Failed to create application icon: " + e.getMessage());
            }
           
            stage.setScene(scene);
           
            // Center the window on screen
            stage.centerOnScreen();
           
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
           
            // Show error dialog with Bootstrap styling
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Application Error");
            alert.setHeaderText("Failed to start the application");
            alert.setContentText(e.getMessage());
           
            // Add Bootstrap styles to the alert
            DialogPane dialogPane = alert.getDialogPane();
            dialogPane.getStylesheets().add(BootstrapFX.bootstrapFXStylesheet());
            dialogPane.getStyleClass().addAll("alert", "alert-danger");
           
            alert.showAndWait();
        }
    }

    /**
     * Main entry point for the JavaFX application.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        launch();
    }
}
