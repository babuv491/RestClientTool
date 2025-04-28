package com.restclient.restclient;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
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

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;
import java.util.Optional;
import java.util.stream.Collectors;

public class RestClient extends Application {
    // Constants
    private static final double PADDING = 10;
    private static final double URL_FIELD_WIDTH = 600;
    private static final double WINDOW_WIDTH = 1200;
    private static final double WINDOW_HEIGHT = 800;
    private static final String APP_TITLE = "REST API Client";
    private static final String ICON_PATH = "src/main/resources/rest_api.png";

    // UI Components
    private final ComboBox<String> methodComboBox = new ComboBox<>();
    private final TextField urlField = new TextField();
    private final TextArea requestBody = new TextArea();
    private final TextArea responseBody = new TextArea();
    private TableView<Header> headersTable = new TableView<>();
    private TableView<QueryParam> queryParamsTable = new TableView<>();
    private ComboBox<String> authTypeComboBox = new ComboBox<>();
    private VBox authDetailsBox = new VBox(10);
    private ListView<HistoryEntry> historyListView = new ListView<>();

    // Observable Collections
    private ObservableList<HistoryEntry> historyList = FXCollections.observableArrayList();
    private final ObservableList<Header> headers = FXCollections.observableArrayList();
    private final ObservableList<QueryParam> queryParams = FXCollections.observableArrayList();

    // Data Models







    @Override
    public void start(Stage primaryStage) {
        initializePrimaryStage(primaryStage);
        Scene scene = createMainScene();
        primaryStage.setScene(scene);
        primaryStage.show();

        // Add URL field listener to detect query parameters
        urlField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && newValue.contains("?")) {
                String queryString = newValue.substring(newValue.indexOf("?") + 1);
                String[] params = queryString.split("&");

                // Clear existing query params
                queryParams.clear();

                // Add each query parameter found in URL
                for (String param : params) {
                    String[] keyValue = param.split("=");
                    if (keyValue.length > 0 ) {
                        queryParams.add(new QueryParam(keyValue[0], keyValue.length > 1 ? keyValue[1] : ""));
                    }
                }

                // Update URL field to remove query parameters
               // urlField.setText(newValue.substring(0, newValue.indexOf("?")));
            }
        });
    }

    private void initializePrimaryStage(Stage primaryStage) {
        primaryStage.setTitle(APP_TITLE);
        loadApplicationIcon(primaryStage);
    }

    private void loadApplicationIcon(Stage primaryStage) {
        try {
            Path iconPath = Path.of(ICON_PATH);
            Image icon = new Image(iconPath.toUri().toString());
            primaryStage.getIcons().add(icon);
        } catch (Exception e) {
            System.err.println("Failed to load application icon: " + e.getMessage());
        }
        primaryStage.show();
    }


    private Scene createMainScene() {
        BorderPane mainLayout = new BorderPane();
        mainLayout.setPadding(new Insets(PADDING));

        mainLayout.setTop(createTopSection());
        mainLayout.setCenter(createCenterSection());
        mainLayout.setRight(createHistorySection());

        return new Scene(mainLayout, WINDOW_WIDTH, WINDOW_HEIGHT);
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

    private void loadHistoryEntry() {
        HistoryEntry entry = historyListView.getSelectionModel().getSelectedItem();
        if (entry != null) {
            methodComboBox.setValue(entry.getMethod());
            urlField.setText(entry.getUrl());
        }
    }
    private HBox createTopSection() {
        HBox topSection = new HBox(10);
        topSection.setAlignment(Pos.CENTER_LEFT);
        topSection.setPadding(new Insets(0, 0, PADDING, 0));

        initializeMethodComboBox();
        initializeUrlField();
        Button sendButton = createSendButton();

        topSection.getChildren().addAll(methodComboBox, urlField, sendButton);
        return topSection;
    }

    private void initializeMethodComboBox() {
        methodComboBox.setItems(FXCollections.observableArrayList(
            "GET", "POST", "PUT", "DELETE", "PATCH", "HEAD", "OPTIONS"
        ));
        methodComboBox.setValue("GET");
    }

    private void initializeUrlField() {
        urlField.setPrefWidth(URL_FIELD_WIDTH);
        urlField.setPromptText("Enter URL");
    }

    private Button createSendButton() {
        Button sendButton = new Button("Send");
        sendButton.setOnAction(e -> {
            try {
                sendRequest();
            } catch (NoSuchAlgorithmException ex) {
                throw new RuntimeException(ex);
            } catch (KeyManagementException ex) {
                throw new RuntimeException(ex);
            }
        });
        return sendButton;
    }


    private SplitPane createCenterSection() {
        SplitPane centerSection = new SplitPane();
        centerSection.getItems().addAll(
            createRequestSection(),
            createResponseSection()
        );
        return centerSection;
    }

    private VBox createRequestSection() {
        VBox requestSection = new VBox(10);
        requestSection.setPadding(new Insets(PADDING));
        requestSection.getChildren().addAll(
            createAuthenticationSection(),
            createHeadersSection(),
            createQueryParamsSection()
        );
        return requestSection;
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

    private void addQueryParam() {

        // Create a dialog
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Add Query Parameter");
        dialog.setHeaderText("Enter Query Param key and value");

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
        result.ifPresent(pair -> queryParams.add(new QueryParam(pair.getKey(), pair.getValue())));

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
    private VBox createResponseSection() {
        VBox responseSection = new VBox(10);
        responseSection.setPadding(new Insets(PADDING));
        responseSection.getChildren().addAll(
                new Label("Request Body"),
                requestBody,
            new Label("Response"),
            responseBody
        );
        return responseSection;
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

    private static TrustManager[] trustAllCerts = new TrustManager[]{
            new X509TrustManager() {
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
                public void checkClientTrusted(
                        java.security.cert.X509Certificate[] certs, String authType) {
                }
                public void checkServerTrusted(
                        java.security.cert.X509Certificate[] certs, String authType) {
                }
            }
    };
    private void sendRequest() throws NoSuchAlgorithmException, KeyManagementException {
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, trustAllCerts, new SecureRandom());
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(10 * 1000))
        .sslContext(sslContext) // SSL context 'sc' initialised as earlier
               // .sslParameters(parameters) // ssl parameters if overriden
                .build();
        try {
           // HttpClient client = HttpClient.newHttpClient();
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
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            responseBody.setText(gson.toJson((new JsonParser().parse(response.body()))));


            // Add to history
            historyList.add(0, new HistoryEntry(
                    methodComboBox.getValue(), urlField.getText()
            ));

        } catch (Exception e) {
            responseBody.setText("Error: " + e.getMessage());
        }
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
