// MainApplication.java
package com.restclient.restclient;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;

public class MainApplication extends Application {

    private ComboBox<String> methodComboBox;
    private TextField urlField;
    private TabPane authTabPane;
    private TabPane requestTabPane;
    private TextArea responseArea;
    private TextArea requestBodyArea;

    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();
        
        // Top section - URL and method
        HBox topSection = createTopSection();
        
        // Center section - Split pane for request and response
        SplitPane centerSection = createCenterSection();
        
        root.setTop(topSection);
        root.setCenter(centerSection);

        Scene scene = new Scene(root, 1200, 800);
        primaryStage.setTitle("API Testing Tool");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private HBox createTopSection() {
        HBox topSection = new HBox(10);
        topSection.setPadding(new Insets(10));

        methodComboBox = new ComboBox<>();
        methodComboBox.getItems().addAll("GET", "POST", "PUT", "DELETE", "PATCH");
        methodComboBox.setValue("GET");

        urlField = new TextField();
        urlField.setPromptText("Enter URL");
        urlField.setPrefWidth(600);

        Button sendButton = new Button("Send");
        sendButton.setOnAction(e -> sendRequest());

        topSection.getChildren().addAll(methodComboBox, urlField, sendButton);
        return topSection;
    }

    private SplitPane createCenterSection() {
        SplitPane splitPane = new SplitPane();
        splitPane.setOrientation(Orientation.HORIZONTAL);

        // Left side - Request configuration
        VBox requestConfig = createRequestConfig();

        // Right side - Response
        VBox responseConfig = createResponseConfig();

        splitPane.getItems().addAll(requestConfig, responseConfig);
        splitPane.setDividerPositions(0.5);
        
        return splitPane;
    }

    private VBox createRequestConfig() {
        VBox requestConfig = new VBox(10);
        requestConfig.setPadding(new Insets(10));

        // Authentication tabs
        authTabPane = new TabPane();
        Tab noAuthTab = new Tab("No Auth", createNoAuthContent());
        Tab basicAuthTab = new Tab("Basic Auth", createBasicAuthContent());
        Tab bearerTab = new Tab("Bearer", createBearerAuthContent());
        Tab awsTab = new Tab("AWS", createAwsAuthContent());
        
        authTabPane.getTabs().addAll(noAuthTab, basicAuthTab, bearerTab, awsTab);

        // Request tabs
        requestTabPane = new TabPane();
        Tab headersTab = new Tab("Headers", createHeadersContent());
        Tab paramsTab = new Tab("Params", createParamsContent());
        Tab bodyTab = new Tab("Body", createBodyContent());
        
        requestTabPane.getTabs().addAll(headersTab, paramsTab, bodyTab);

        requestConfig.getChildren().addAll(new Label("Authentication"), authTabPane, 
                                         new Label("Request"), requestTabPane);
        return requestConfig;
    }

    private VBox createResponseConfig() {
        VBox responseConfig = new VBox(10);
        responseConfig.setPadding(new Insets(10));

        responseArea = new TextArea();
        responseArea.setWrapText(true);
        responseArea.setEditable(false);
        responseArea.setPrefRowCount(20);

        responseConfig.getChildren().addAll(new Label("Response"), responseArea);
        return responseConfig;
    }

    private VBox createNoAuthContent() {
        return new VBox(new Label("No authentication required"));
    }

    private VBox createBasicAuthContent() {
        VBox content = new VBox(10);
        TextField usernameField = new TextField();
        PasswordField passwordField = new PasswordField();
        usernameField.setPromptText("Username");
        passwordField.setPromptText("Password");
        content.getChildren().addAll(
            new Label("Username:"), usernameField,
            new Label("Password:"), passwordField
        );
        return content;
    }

    private VBox createBearerAuthContent() {
        VBox content = new VBox(10);
        TextField tokenField = new TextField();
        tokenField.setPromptText("Bearer Token");
        content.getChildren().addAll(new Label("Token:"), tokenField);
        return content;
    }

    private VBox createAwsAuthContent() {
        VBox content = new VBox(10);
        TextField accessKeyField = new TextField();
        PasswordField secretKeyField = new PasswordField();
        TextField regionField = new TextField();
        
        accessKeyField.setPromptText("Access Key ID");
        secretKeyField.setPromptText("Secret Access Key");
        regionField.setPromptText("Region");
        
        content.getChildren().addAll(
            new Label("Access Key ID:"), accessKeyField,
            new Label("Secret Access Key:"), secretKeyField,
            new Label("Region:"), regionField
        );
        return content;
    }

    private VBox createHeadersContent() {
        VBox content = new VBox(10);
        TableView<HeaderEntry> headerTable = new TableView<>();
        // Add header table columns and functionality
        return content;
    }

    private VBox createParamsContent() {
        VBox content = new VBox(10);
        TableView<ParamEntry> paramTable = new TableView<>();
        // Add parameter table columns and functionality
        return content;
    }

    private VBox createBodyContent() {
        VBox content = new VBox(10);
        requestBodyArea = new TextArea();
        requestBodyArea.setPromptText("Request Body (JSON, XML, etc.)");
        content.getChildren().add(requestBodyArea);
        return content;
    }

private void sendRequest() {
    String method = methodComboBox.getValue();
    String url = urlField.getText();
    String requestBody = requestBodyArea.getText();

    // Validate URL
    if (url == null || url.trim().isEmpty()) {
        responseArea.setText("Error: Please enter a valid URL");
        return;
    }

    // Create HTTP client and send request asynchronously
    HttpClient client = HttpClient.newHttpClient();
    HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
        .uri(URI.create(url));

    // Set method and body
    switch (method) {
        case "GET":
            requestBuilder.GET();
            break;
        case "POST":
            requestBuilder.POST(HttpRequest.BodyPublishers.ofString(requestBody));
            break;
        case "PUT":
            requestBuilder.PUT(HttpRequest.BodyPublishers.ofString(requestBody));
            break;
        case "DELETE":
            requestBuilder.DELETE();
            break;
        case "PATCH":
            requestBuilder.method("PATCH", HttpRequest.BodyPublishers.ofString(requestBody));
            break;
    }

    // Add authentication headers based on selected auth tab
    Tab selectedAuthTab = authTabPane.getSelectionModel().getSelectedItem();
    if (selectedAuthTab != null) {
        switch (selectedAuthTab.getText()) {
            case "Basic Auth":
                VBox basicAuthContent = (VBox) selectedAuthTab.getContent();
                TextField usernameField = (TextField) basicAuthContent.getChildren().get(1);
                PasswordField passwordField = (PasswordField) basicAuthContent.getChildren().get(3);
                String auth = usernameField.getText() + ":" + passwordField.getText();
                String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
                requestBuilder.header("Authorization", "Basic " + encodedAuth);
                break;
            case "Bearer":
                VBox bearerContent = (VBox) selectedAuthTab.getContent();
                TextField tokenField = (TextField) bearerContent.getChildren().get(1);
                requestBuilder.header("Authorization", "Bearer " + tokenField.getText());
                break;
        }
    }

    // Send request asynchronously
    client.sendAsync(requestBuilder.build(), HttpResponse.BodyHandlers.ofString())
        .thenAccept(response -> {
            // Update response area on JavaFX thread
            Platform.runLater(() -> {
                responseArea.setText("Status: " + response.statusCode() + "\n\n" + response.body());
            });
        })
        .exceptionally(e -> {
            Platform.runLater(() -> {
                responseArea.setText("Error: " + e.getMessage());
            });
            return null;
        });
}
    public static void main(String[] args) {
        launch(args);
    }
}

// Helper classes
class HeaderEntry {
    private String key;
    private String value;
    private boolean enabled;
    // Add getters, setters, and constructors
}

class ParamEntry {
    private String key;
    private String value;
    private boolean enabled;
    // Add getters, setters, and constructors
}
