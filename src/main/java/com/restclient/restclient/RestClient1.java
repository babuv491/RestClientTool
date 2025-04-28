package com.restclient.restclient;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Date;
import java.util.Optional;
import java.util.stream.Collectors;

public class RestClient1 extends Application {
    private static final double PADDING = 10;
    private static final double URL_FIELD_WIDTH = 600;
    private static final double WINDOW_WIDTH = 1200;
    private static final double WINDOW_HEIGHT = 800;

    private ComboBox<String> methodComboBox;
    private TextField urlField;
    private TextArea requestBody;
    private TextArea responseBody;
    private TableView<Header> headersTable;
    private TableView<QueryParam> queryParamsTable;
    private ComboBox<String> authTypeComboBox;
    private VBox authDetailsBox;
    private ListView<HistoryEntry> historyListView;
    private ObservableList<HistoryEntry> historyList;
    private ObservableList<Header> headers = FXCollections.observableArrayList();
    private ObservableList<QueryParam> queryParams = FXCollections.observableArrayList();

    // Inner classes for data models
    private static class Header {
        private String key;
        private String value;
        
        public Header(String key, String value) {
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    private static class QueryParam {
        private String key;
        private String value;

        public QueryParam(String key, String value) {
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    private static class HistoryEntry {
        private String method;
        private String url;
        private String timestamp;

        public HistoryEntry(String method, String url) {
            this.method = method;
            this.url = url;
            this.timestamp = new Date().toString();
        }

        @Override
        public String toString() {
            return String.format("[%s] %s - %s", method, url, timestamp);
        }
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("REST API Client");

        try{
            Path iconPath = Path.of("src/main/resources/rest_api.png");
            Image icon = new Image(iconPath.toUri().toString());
            primaryStage.getIcons().add(icon);

        }catch (Exception e){
            e.printStackTrace();
        }

        // Create main layout
        BorderPane mainLayout = new BorderPane();
        mainLayout.setPadding(new Insets(PADDING));

        // Create top section with URL and method
        HBox topSection = createTopSection();
        mainLayout.setTop(topSection);

        // Create center section with request/response
        SplitPane centerSection = createCenterSection();
        mainLayout.setCenter(centerSection);

        // Create right section with history
        VBox rightSection = createHistorySection();
        mainLayout.setRight(rightSection);

        Scene scene = new Scene(mainLayout, WINDOW_WIDTH, WINDOW_HEIGHT);

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private HBox createTopSection() {
        HBox topSection = new HBox(10);
        topSection.setAlignment(Pos.CENTER_LEFT);
        topSection.setPadding(new Insets(0, 0, PADDING, 0));

        // HTTP Method selector
        methodComboBox = new ComboBox<>(FXCollections.observableArrayList(
            "GET", "POST", "PUT", "DELETE", "PATCH", "HEAD", "OPTIONS"
        ));
        methodComboBox.setValue("GET");

        // URL field
        urlField = new TextField();
        urlField.setPrefWidth(URL_FIELD_WIDTH);
        urlField.setPromptText("Enter URL");

        // Send button
        Button sendButton = new Button("Send");
        sendButton.setOnAction(e -> sendRequest());

        topSection.getChildren().addAll(methodComboBox, urlField, sendButton);
        return topSection;
    }

    private SplitPane createCenterSection() {
        SplitPane centerSection = new SplitPane();

        // Left side - Request
        VBox requestSection = new VBox(10);
        requestSection.setPadding(new Insets(PADDING));

        // Authentication section
        VBox authSection = createAuthenticationSection();

        // Headers section
        VBox headersSection = createHeadersSection();

        // Query Parameters section
        VBox queryParamsSection = createQueryParamsSection();

        // Request body
        requestBody = new TextArea();
        requestBody.setPromptText("Request Body (JSON)");

        Button formatRequestButton = new Button("Format Request");
        formatRequestButton.setOnAction(e -> formatJson(requestBody));

        requestSection.getChildren().addAll(
            authSection, headersSection, queryParamsSection
        );

        // Right side - Response
        VBox responseSection = new VBox(10);
        responseSection.setPadding(new Insets(PADDING));

        responseBody = new TextArea();
        responseBody.setEditable(false);
        responseBody.setPromptText("Response will appear here");

        Button formatResponseButton = new Button("Format Response");
        formatResponseButton.setOnAction(e -> formatJson(responseBody));

        responseSection.getChildren().addAll(new Label("Request Body"), requestBody, formatRequestButton,
            new Label("Response"), responseBody, formatResponseButton
        );

        centerSection.getItems().addAll(requestSection, responseSection);
        return centerSection;
    }

    private VBox createAuthenticationSection() {
        VBox authSection = new VBox(10);

        authTypeComboBox = new ComboBox<>(FXCollections.observableArrayList(
            "None", "Basic Auth", "Bearer Token", "AWS Signature", "API Key"
        ));
        authTypeComboBox.setValue("None");

        authDetailsBox = new VBox(10);
        authTypeComboBox.setOnAction(e -> updateAuthenticationFields());

        authSection.getChildren().addAll(
            new Label("Authentication"), authTypeComboBox, authDetailsBox
        );
        return authSection;
    }

    private void updateAuthenticationFields() {
        authDetailsBox.getChildren().clear();

        switch (authTypeComboBox.getValue()) {
            case "Basic Auth":
                TextField usernameField = new TextField();
                PasswordField passwordField = new PasswordField();
                usernameField.setPromptText("Username");
                passwordField.setPromptText("Password");
                authDetailsBox.getChildren().addAll(usernameField, passwordField);
                break;

            case "Bearer Token":
                TextField tokenField = new TextField();
                tokenField.setPromptText("Bearer Token");
                authDetailsBox.getChildren().add(tokenField);
                break;

            case "AWS Signature":
                // AWS credentials should be loaded from environment variables
                Label awsNote = new Label("Using AWS credentials from environment");
                authDetailsBox.getChildren().add(awsNote);
                break;

            case "API Key":
                TextField apiKeyField = new TextField();
                TextField apiKeyHeaderField = new TextField();
                apiKeyField.setPromptText("API Key");
                apiKeyHeaderField.setPromptText("Header Name (default: X-API-Key)");
                authDetailsBox.getChildren().addAll(apiKeyHeaderField, apiKeyField);
                break;
        }
    }

    private VBox createHeadersSection() {
        VBox headersSection = new VBox(10);
        headersTable = new TableView<>();

        TableColumn<Header, String> keyColumn = new TableColumn<>("Key");
        keyColumn.setCellValueFactory(new PropertyValueFactory<>("key"));
        keyColumn.setCellFactory(TextFieldTableCell.forTableColumn());

        TableColumn<Header, String> valueColumn = new TableColumn<>("Value");
        valueColumn.setCellValueFactory(new PropertyValueFactory<>("value"));
        valueColumn.setCellFactory(TextFieldTableCell.forTableColumn());

        headersTable.getColumns().addAll(keyColumn, valueColumn);
        headersTable.setItems(headers);
        headersTable.setEditable(true);

        Button addHeaderButton = new Button("Add Header");
        addHeaderButton.setOnAction(e -> addHeader());

        headersSection.getChildren().addAll(
            new Label("Headers"), headersTable, addHeaderButton
        );
        return headersSection;
    }

    private VBox createQueryParamsSection() {
        VBox queryParamsSection = new VBox(10);
        queryParamsTable = new TableView<>();

        TableColumn<QueryParam, String> keyColumn = new TableColumn<>("Key");
        keyColumn.setCellValueFactory(new PropertyValueFactory<>("key"));
        keyColumn.setCellFactory(TextFieldTableCell.forTableColumn());

        TableColumn<QueryParam, String> valueColumn = new TableColumn<>("Value");
        valueColumn.setCellValueFactory(new PropertyValueFactory<>("value"));
        valueColumn.setCellFactory(TextFieldTableCell.forTableColumn());

        queryParamsTable.getColumns().addAll(keyColumn, valueColumn);
        queryParamsTable.setItems(queryParams);
        queryParamsTable.setEditable(true);

        Button addParamButton = new Button("Add Parameter");
        addParamButton.setOnAction(e -> addQueryParam());

        queryParamsSection.getChildren().addAll(
            new Label("Query Parameters"), queryParamsTable, addParamButton
        );
        return queryParamsSection;
    }

    private VBox createHistorySection() {
        VBox historySection = new VBox(10);
        historySection.setPadding(new Insets(PADDING));
        historySection.setPrefWidth(300);

        historyList = FXCollections.observableArrayList();
        historyListView = new ListView<>(historyList);
        historyListView.setOnMouseClicked(e -> loadHistoryEntry());

        historySection.getChildren().addAll(
            new Label("History"), historyListView
        );
        return historySection;
    }

    private void sendRequest() {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(new URI(buildUrlWithParams()))
                .method(methodComboBox.getValue(),
                       HttpRequest.BodyPublishers.ofString(requestBody.getText()));

            // Add headers
            addHeadersToRequest(requestBuilder);

            // Add authentication
            addAuthenticationToRequest(requestBuilder);

            HttpResponse<String> response = client.send(requestBuilder.build(),
                HttpResponse.BodyHandlers.ofString());

            responseBody.setText(response.body());

            // Add to history
            historyList.add(0, new HistoryEntry(
                methodComboBox.getValue(), urlField.getText()
            ));

        } catch (Exception e) {
            responseBody.setText("Error: " + e.getMessage());
        }
    }

    private void formatJson(TextArea textArea) {
        try {
            String text = textArea.getText();
            if (!text.isEmpty()) {
               // JSONObject json = new JSONObject(text);
                //textArea.setText(json.toString(4));
                textArea.setText(text);
            }
        } catch (Exception e) {
            // If formatting fails, keep original text
        }
    }

    private void loadHistoryEntry() {
        HistoryEntry entry = historyListView.getSelectionModel().getSelectedItem();
        if (entry != null) {
            methodComboBox.setValue(entry.method);
            urlField.setText(entry.url);
        }
    }

private void addHeader() {
    // Create a dialog
    Dialog<Pair<String, String>> dialog = new Dialog<>();
    dialog.setTitle("Add Header");
    dialog.setHeaderText("Enter header key and value");

    // Set the button types
    ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
    dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

    // Create the key and value fields
    GridPane grid = new GridPane();
    grid.setHgap(10);
    grid.setVgap(10);
    grid.setPadding(new Insets(20, 150, 10, 10));

    TextField keyField = new TextField();
    keyField.setPromptText("Key");
    TextField valueField = new TextField();
    valueField.setPromptText("Value");

    grid.add(new Label("Key:"), 0, 0);
    grid.add(keyField, 1, 0);
    grid.add(new Label("Value:"), 0, 1);
    grid.add(valueField, 1, 1);

    dialog.getDialogPane().setContent(grid);

    // Convert the result to a key-value pair when the add button is clicked
    dialog.setResultConverter(dialogButton -> {
        if (dialogButton == addButtonType) {
            return new Pair<>(keyField.getText(), valueField.getText());
        }
        return null;
    });

    // Show the dialog and add the header
    Optional<Pair<String, String>> result = dialog.showAndWait();
    result.ifPresent(pair -> headers.add(new Header(pair.getKey(), pair.getValue())));
}

    private void addQueryParam() {
        queryParams.add(new QueryParam("", ""));
    }

    private String buildUrlWithParams() {
        String baseUrl = urlField.getText();
        if (queryParams.isEmpty()) {
            return baseUrl;
        }

        String queryString = queryParams.stream()
            .filter(param -> !param.getKey().isEmpty())
            .map(param -> param.getKey() + "=" + param.getValue())
            .collect(Collectors.joining("&"));

        return baseUrl + (baseUrl.contains("?") ? "&" : "?") + queryString;
    }

    private void addHeadersToRequest(HttpRequest.Builder requestBuilder) {
        headers.stream()
            .filter(header -> !header.getKey().isEmpty())
            .forEach(header -> requestBuilder.header(header.getKey(), header.getValue()));
    }

    private void addAuthenticationToRequest(HttpRequest.Builder requestBuilder) {
        switch (authTypeComboBox.getValue()) {
            case "Basic Auth":
                TextField usernameField = (TextField) authDetailsBox.getChildren().get(0);
                PasswordField passwordField = (PasswordField) authDetailsBox.getChildren().get(1);
                String auth = usernameField.getText() + ":" + passwordField.getText();
                String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
                requestBuilder.header("Authorization", "Basic " + encodedAuth);
                break;

            case "Bearer Token":
                TextField tokenField = (TextField) authDetailsBox.getChildren().get(0);
                requestBuilder.header("Authorization", "Bearer " + tokenField.getText());
                break;

            case "API Key":
                TextField headerField = (TextField) authDetailsBox.getChildren().get(0);
                TextField keyField = (TextField) authDetailsBox.getChildren().get(1);
                String headerName = headerField.getText().isEmpty() ? "X-API-Key" : headerField.getText();
                requestBuilder.header(headerName, keyField.getText());
                break;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}