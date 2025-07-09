package com.rct.restclienttool;

/**
* Alternative launcher class for the REST Client Tool application.
* Provides JavaFX system property configuration and error handling.
*
 * @author REST Client Tool
* @version 1.0
* @since 1.0
*/
public class Launcher {
    /**
     * Main entry point with JavaFX configuration and error handling.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        // Set JavaFX system properties to avoid module issues
        System.setProperty("java.awt.headless", "false");
       
        try {
            // Launch JavaFX application without module system
            javafx.application.Application.launch(RestClientApplication.class, args);
        } catch (Exception e) {
            System.err.println("Failed to start application: " + e.getMessage());
            e.printStackTrace();
        }
    }
}