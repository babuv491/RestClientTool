package com.rct.restclienttool;

import com.rct.restclienttool.model.*;
import com.rct.restclienttool.service.HttpClientService;
import com.rct.restclienttool.service.RequestHistoryService;
import com.rct.restclienttool.util.CurlImporter;
import com.rct.restclienttool.util.ValidationUtils;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.kordamp.bootstrapfx.BootstrapFX;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
* Controller for individual request tabs in the main application.
* Handles HTTP request configuration, execution, and validation.
*
 * @author REST Client Tool
* @version 1.0
* @since 1.0
*/
public class RequestTabController {
    private static final Logger LOGGER = Logger.getLogger(RequestTabController.class.getName());
   
    @FXML private VBox requestTabRoot;
    @FXML private ComboBox<String> methodComboBox;
    @FXML private TextField urlField;
    @FXML private Button sendButton;
    @FXML private HBox loadingContainer;
    @FXML private ProgressIndicator loadingIndicator;
    @FXML private javafx.scene.shape.SVGPath sendIcon;
   
    @FXML private TableView<HeaderRow> headersTable;
    @FXML private TableColumn<HeaderRow, String> headerNameColumn;
    @FXML private TableColumn<HeaderRow, String> headerValueColumn;
    @FXML private TableColumn<HeaderRow, Boolean> headerEnabledColumn;
   
    @FXML private TableView<ParamRow> paramsTable;
    @FXML private TableColumn<ParamRow, String> paramNameColumn;
    @FXML private TableColumn<ParamRow, String> paramValueColumn;
    @FXML private TableColumn<ParamRow, Boolean> paramEnabledColumn;
   
    @FXML private ComboBox<String> bodyTypeComboBox;
    @FXML private TextArea bodyTextArea;
   
    @FXML private ComboBox<AuthType> authTypeComboBox;
    @FXML private VBox basicAuthPane;
    @FXML private TextField basicAuthUsername;
    @FXML private PasswordField basicAuthPassword;
    @FXML private VBox bearerAuthPane;
    @FXML private TextField bearerToken;
    @FXML private VBox apiKeyAuthPane;
    @FXML private TextField apiKeyName;
    @FXML private TextField apiKeyValue;
    @FXML private ComboBox<String> apiKeyLocation;
    @FXML private VBox oauth2AuthPane;
    @FXML private TextField oauth2Token;
   
    private final HttpClientService httpClientService = new HttpClientService();
    private final RequestHistoryService historyService = new RequestHistoryService();
    private final ObservableList<HeaderRow> headers = FXCollections.observableArrayList();
    private final ObservableList<ParamRow> params = FXCollections.observableArrayList();
    private final StringBuilder requestLogs = new StringBuilder();
   
    private RestClientController parentController;
   
    /**
     * Initializes the request tab controller and sets up UI components.
     * Called automatically by JavaFX after FXML loading.
     */
    @FXML
    public void initialize() {
        // Set up HTTP methods
        methodComboBox.setItems(FXCollections.observableArrayList(
                "GET", "POST", "PUT", "DELETE", "PATCH", "HEAD", "OPTIONS"));
        methodComboBox.setValue("GET");
       
        // Add context menu for URL field with curl import option
        ContextMenu contextMenu = new ContextMenu();
        MenuItem importCurlItem = new MenuItem("Import from cURL");
        importCurlItem.setOnAction(e -> handleImportCurl());
        contextMenu.getItems().add(importCurlItem);
        urlField.setContextMenu(contextMenu);
       
        // Add URL validation and query parameter sync on focus lost
        urlField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal && !urlField.getText().isEmpty()) { // Focus lost and field not empty
                // Validate URL
                ValidationUtils.ValidationResult result = ValidationUtils.validateUrl(urlField.getText());
                if (!result.isValid()) {
                    urlField.setStyle("-fx-border-color: #dc3545;"); // Bootstrap danger color
                    Tooltip tooltip = new Tooltip(result.getMessage());
                    tooltip.getStyleClass().add("tooltip-danger");
                    urlField.setTooltip(tooltip);
                } else {
                    urlField.setStyle(""); // Reset style
                    urlField.setTooltip(null);
                   
                    // Sync query parameters from URL to table
                    syncQueryParamsFromUrl();
                }
            }
        });
       
        // Add listener for URL text changes to sync query parameters
        urlField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.isEmpty() && newVal.contains("?")) {
                // Only sync when URL contains query parameters and has focus
                // to avoid recursive updates
                if (urlField.isFocused()) {
                    syncQueryParamsFromUrl();
                }
            }
        });
       
        // Add method styling using BootstrapFX
        methodComboBox.setCellFactory(listView -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item);
                    getStyleClass().removeAll("text-primary", "text-success", "text-warning", "text-danger", "text-info");
                   
                    // Apply Bootstrap colors based on method
                    switch (item) {
                        case "GET":
                            getStyleClass().add("text-primary");
                            break;
                        case "POST":
                            getStyleClass().add("text-success");
                            break;
                        case "PUT":
                            getStyleClass().add("text-warning");
                            break;
                        case "DELETE":
                            getStyleClass().add("text-danger");
                            break;
                        default:
                            getStyleClass().add("text-info");
                            break;
                    }
                }
            }
        });
       
        methodComboBox.setButtonCell(new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item);
                    getStyleClass().removeAll("text-primary", "text-success", "text-warning", "text-danger", "text-info");
                   
                    // Apply Bootstrap colors based on method
                    switch (item) {
                        case "GET":
                            getStyleClass().add("text-primary");
                            break;
                        case "POST":
                            getStyleClass().add("text-success");
                            break;
                        case "PUT":
                            getStyleClass().add("text-warning");
                            break;
                        case "DELETE":
                            getStyleClass().add("text-danger");
                            break;
                        default:
                            getStyleClass().add("text-info");
                            break;
                    }
                }
            }
        });
       
        // Set up body content types
        bodyTypeComboBox.setItems(FXCollections.observableArrayList(
                "application/json", "application/xml", "text/plain",
                "application/x-www-form-urlencoded", "multipart/form-data"));
        bodyTypeComboBox.setValue("application/json");
       
        // Add validation when content type changes
        bodyTypeComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            String body = bodyTextArea.getText();
            if (body != null && !body.isEmpty()) {
                ValidationUtils.ValidationResult result = ValidationUtils.validateBody(body, newVal);
                if (!result.isValid()) {
                    showValidationAlert("The current body content may not be valid for the selected content type: " +
                                       result.getMessage());
                }
            }
        });
       
        // Add real-time validation for body content
        bodyTextArea.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.isEmpty()) {
                String contentType = bodyTypeComboBox.getValue();
                if (contentType != null) {
                    if (contentType.contains("json")) {
                        // Only validate JSON in real-time if it ends with } or ]
                        if (newVal.trim().endsWith("}") || newVal.trim().endsWith("]")) {
                            ValidationUtils.ValidationResult result = ValidationUtils.validateJson(newVal);
                            if (!result.isValid()) {
                                bodyTextArea.setStyle("-fx-border-color: #ffc107;"); // Warning color
                            } else {
                                bodyTextArea.setStyle(""); // Reset style
                            }
                        }
                    } else if (contentType.contains("xml")) {
                        // Only validate XML in real-time if it ends with >
                        if (newVal.trim().endsWith(">")) {
                            ValidationUtils.ValidationResult result = ValidationUtils.validateXml(newVal);
                            if (!result.isValid()) {
                                bodyTextArea.setStyle("-fx-border-color: #ffc107;"); // Warning color
                            } else {
                                bodyTextArea.setStyle(""); // Reset style
                            }
                        }
                    }
                }
            } else {
                bodyTextArea.setStyle(""); // Reset style when empty
            }
        });
       
        // Set up auth types
        authTypeComboBox.setItems(FXCollections.observableArrayList(AuthType.values()));
        authTypeComboBox.setValue(AuthType.NONE);
        authTypeComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            updateAuthPanes(newVal);
        });
       
        // Set up API key locations
        apiKeyLocation.getItems().addAll("header", "query");
        apiKeyLocation.setValue("header");
       
        // Set up headers table
        headerNameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        headerValueColumn.setCellValueFactory(cellData -> cellData.getValue().valueProperty());
        headerEnabledColumn.setCellValueFactory(cellData -> cellData.getValue().enabledProperty());
       
        headerNameColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        headerValueColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        headerEnabledColumn.setCellFactory(CheckBoxTableCell.forTableColumn(headerEnabledColumn));
       
        headerNameColumn.setOnEditCommit(event -> {
            String newValue = event.getNewValue();
            HeaderRow row = event.getRowValue();
           
            // Validate header name
            if (newValue == null || newValue.trim().isEmpty()) {
                showValidationAlert("Header name cannot be empty");
                row.setName(""); // Set to empty string instead of null
            } else if (newValue.contains(":") || newValue.contains("\n") || newValue.contains("\r")) {
                showValidationAlert("Header name contains invalid characters: " + newValue);
                // Still set the value but warn the user
                row.setName(newValue);
            } else {
                row.setName(newValue);
            }
        });
       
        headerValueColumn.setOnEditCommit(event -> {
            event.getRowValue().setValue(event.getNewValue() != null ? event.getNewValue() : "");
        });
       
        headersTable.setItems(headers);
        headersTable.setEditable(true);
       
        // Set up params table
        paramNameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        paramValueColumn.setCellValueFactory(cellData -> cellData.getValue().valueProperty());
        paramEnabledColumn.setCellValueFactory(cellData -> cellData.getValue().enabledProperty());
       
        paramNameColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        paramValueColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        paramEnabledColumn.setCellFactory(CheckBoxTableCell.forTableColumn(paramEnabledColumn));
       
        paramNameColumn.setOnEditCommit(event -> {
            String newValue = event.getNewValue();
            ParamRow row = event.getRowValue();
           
            // Validate parameter name
            if (newValue == null || newValue.trim().isEmpty()) {
                showValidationAlert("Parameter name cannot be empty");
                row.setName(""); // Set to empty string instead of null
            } else if (newValue.contains("=") || newValue.contains("&") ||
                      newValue.contains("?") || newValue.contains(" ")) {
                showValidationAlert("Parameter name contains invalid characters: " + newValue);
                // Still set the value but warn the user
                row.setName(newValue);
            } else {
                row.setName(newValue);
            }
           
            // Update URL after editing parameter name
            updateUrlFromParams();
        });
       
        paramValueColumn.setOnEditCommit(event -> {
            String newValue = event.getNewValue();
            ParamRow row = event.getRowValue();
           
            // Set the value, but warn if it contains spaces or special characters
            if (newValue != null && (newValue.contains(" ") || newValue.contains("&") ||
                                    newValue.contains("?") || newValue.contains("#"))) {
                showValidationAlert("Parameter value contains characters that may need URL encoding: " + newValue);
            }
           
            row.setValue(newValue != null ? newValue : "");
           
            // Update URL after editing parameter value
            updateUrlFromParams();
        });
       
        paramsTable.setItems(params);
        paramsTable.setEditable(true);
       
        // Store controller in the node properties for access from parent
        requestTabRoot.getProperties().put("controller", this);
       
        // Try to find the parent controller
        javafx.application.Platform.runLater(() -> {
            RestClientController controller = getParentController();
            if (controller != null) {
                setParentController(controller);
            }
        });
    }
   
    @FXML
    private void handleSendRequest() {
        System.out.println("Send button clicked");
       
        HttpRequest request = buildRequest();
       
        // Validate the request before sending
        List<ValidationUtils.ValidationResult> validationResults = ValidationUtils.validateRequest(request);
        boolean hasErrors = false;
        StringBuilder errorMessage = new StringBuilder("Please fix the following errors:\n");
       
        for (ValidationUtils.ValidationResult result : validationResults) {
            if (!result.isValid()) {
                hasErrors = true;
                errorMessage.append("• ").append(result.getMessage()).append("\n");
            }
        }
       
        if (hasErrors) {
            showValidationAlert(errorMessage.toString());
            return;
        }
       
        logRequest(request);
       
        // Check if parent controller is available
        RestClientController parent = getParentController();
        System.out.println("Parent controller: " + (parent != null ? "found" : "not found"));
       
        // Show loading indicator and disable send button
        showLoading(true);
       
        CompletableFuture.supplyAsync(() -> httpClientService.executeRequest(request))
                .thenAccept(response -> {
                    javafx.application.Platform.runLater(() -> {
                        logResponse(response);
                       
                        // Update the response view in the main controller
                        RestClientController parentController = getParentController();
                        if (parentController != null) {
                            parentController.updateResponseView(response);
                        }
                       
                        // Save to history
                        try {
                            RequestHistoryEntry historyEntry = new RequestHistoryEntry();
                            historyEntry.setUrl(request.getUrl());
                            historyEntry.setMethod(request.getMethod());
                           historyEntry.setStatusCode(response.getStatusCode());
                            historyEntry.setResponseTimeMs(response.getResponseTimeMs());
                           
                            // Create simplified copies for serialization
                            HttpRequest requestCopy = new HttpRequest();
                            requestCopy.setUrl(request.getUrl());
                            requestCopy.setMethod(request.getMethod());
                            requestCopy.setBody(request.getBody());
                            requestCopy.setHeaders(new HashMap<>(request.getHeaders()));
                            requestCopy.setAuthType(request.getAuthType());
                           
                            HttpResponse responseCopy = new HttpResponse();
                            responseCopy.setStatusCode(response.getStatusCode());
                            responseCopy.setBody(response.getBody());
                            responseCopy.setHeaders(new HashMap<>(response.getHeaders()));
                            responseCopy.setResponseTimeMs(response.getResponseTimeMs());
                           
                            historyEntry.setRequest(requestCopy);
                            historyEntry.setResponse(responseCopy);
                            historyService.addHistoryEntry(historyEntry);
                        } catch (Exception e) {
                            LOGGER.log(Level.SEVERE, "Failed to save history entry", e);
                        }
                       
                        // Hide loading indicator and re-enable send button
                        showLoading(false);
                    });
                })
                .exceptionally(ex -> {
                    javafx.application.Platform.runLater(() -> {
                        LOGGER.log(Level.SEVERE, "Error executing request", ex);
                        logError(ex);
                        showLoading(false);
                    });
                    return null;
                });
    }
   
    @FXML
    private void handleAddHeader() {
        headers.add(new HeaderRow("", ""));
        headersTable.getSelectionModel().selectLast();
        headersTable.edit(headers.size() - 1, headerNameColumn);
    }
   
    @FXML
    private void handleRemoveHeader() {
        int selectedIndex = headersTable.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0) {
            headers.remove(selectedIndex);
        }
    }
   
    @FXML
    private void handleAddParam() {
        params.add(new ParamRow("", ""));
        paramsTable.getSelectionModel().selectLast();
        paramsTable.edit(params.size() - 1, paramNameColumn);
    }
   
    @FXML
    private void handleRemoveParam() {
        int selectedIndex = paramsTable.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0) {
            params.remove(selectedIndex);
            // Update URL after removing parameter
            updateUrlFromParams();
        }
    }
   
    @FXML
    private void handleFormatBody() {
        String body = bodyTextArea.getText();
        if (body != null && !body.isEmpty()) {
            String contentType = bodyTypeComboBox.getValue();
            if (contentType != null && contentType.contains("json")) {
                // Validate JSON before formatting
                ValidationUtils.ValidationResult result = ValidationUtils.validateJson(body);
                if (!result.isValid()) {
                    showValidationAlert("Invalid JSON: " + result.getMessage());
                    return;
                }
               
                try {
                    // Use the JsonFormatter utility to format JSON
                    String formattedJson = com.rct.restclienttool.util.JsonFormatter.prettyPrint(body);
                    bodyTextArea.setText(formattedJson);
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Failed to format JSON body", e);
                    showValidationAlert("Failed to format JSON: " + e.getMessage());
                }
            } else if (contentType != null && contentType.contains("xml")) {
                // Validate XML before formatting
                ValidationUtils.ValidationResult result = ValidationUtils.validateXml(body);
                if (!result.isValid()) {
                    showValidationAlert("Invalid XML: " + result.getMessage());
                    return;
                }
               
                try {
                    // Format XML (simplified version)
                    javax.xml.transform.TransformerFactory transformerFactory = javax.xml.transform.TransformerFactory.newInstance();
                    javax.xml.transform.Transformer transformer = transformerFactory.newTransformer();
                    transformer.setOutputProperty(javax.xml.transform.OutputKeys.INDENT, "yes");
                    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
                   
                    javax.xml.parsers.DocumentBuilderFactory dbFactory = javax.xml.parsers.DocumentBuilderFactory.newInstance();
                    javax.xml.parsers.DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                    org.w3c.dom.Document doc = dBuilder.parse(new org.xml.sax.InputSource(new java.io.StringReader(body)));
                   
                    java.io.StringWriter writer = new java.io.StringWriter();
                    transformer.transform(new javax.xml.transform.dom.DOMSource(doc), new javax.xml.transform.stream.StreamResult(writer));
                   
                    bodyTextArea.setText(writer.toString());
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Failed to format XML body", e);
                    showValidationAlert("Failed to format XML: " + e.getMessage());
                }
            }
        }
    }
   
    private void showValidationAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Validation Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
       
        // Apply Bootstrap styling
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(BootstrapFX.bootstrapFXStylesheet());
        dialogPane.getStyleClass().addAll("alert", "alert-warning");
       
        alert.showAndWait();
    }
   
    private HttpRequest buildRequest() {
        HttpRequest request = new HttpRequest();
       
        // Set basic request properties
        request.setUrl(buildUrlWithParams());
        request.setMethod(methodComboBox.getValue());
       
        // Set headers
        for (HeaderRow header : headers) {
            if (header.isEnabled() && !header.getName().isEmpty()) {
                request.addHeader(header.getName(), header.getValue());
            }
        }
       
        // Add content type header if body is present
        if (bodyTextArea.getText() != null && !bodyTextArea.getText().isEmpty()) {
            request.addHeader("Content-Type", bodyTypeComboBox.getValue());
        }
       
        // Set body
        request.setBody(bodyTextArea.getText());
       
        // Set authentication
        request.setAuthType(authTypeComboBox.getValue());
       
        // Validate and set auth parameters
        switch (authTypeComboBox.getValue()) {
            case BASIC:
                if (basicAuthUsername.getText().isEmpty()) {
                    basicAuthUsername.setStyle("-fx-border-color: #dc3545;");
                    basicAuthUsername.setTooltip(new Tooltip("Username cannot be empty"));
                } else {
                    basicAuthUsername.setStyle("");
                    basicAuthUsername.setTooltip(null);
                }
               
                request.addAuthParam("username", basicAuthUsername.getText());
                request.addAuthParam("password", basicAuthPassword.getText());
                break;
                
            case BEARER:
                if (bearerToken.getText().isEmpty()) {
                    bearerToken.setStyle("-fx-border-color: #dc3545;");
                    bearerToken.setTooltip(new Tooltip("Token cannot be empty"));
                } else {
                    bearerToken.setStyle("");
                    bearerToken.setTooltip(null);
                }
               
                request.addAuthParam("token", bearerToken.getText());
                break;
               
            case API_KEY:
                boolean hasError = false;
               
                if (apiKeyName.getText().isEmpty()) {
                    apiKeyName.setStyle("-fx-border-color: #dc3545;");
                    apiKeyName.setTooltip(new Tooltip("Key name cannot be empty"));
                    hasError = true;
                } else {
                    apiKeyName.setStyle("");
                    apiKeyName.setTooltip(null);
                }
               
                if (apiKeyValue.getText().isEmpty()) {
                    apiKeyValue.setStyle("-fx-border-color: #dc3545;");
                    apiKeyValue.setTooltip(new Tooltip("Key value cannot be empty"));
                    hasError = true;
                } else {
                    apiKeyValue.setStyle("");
                    apiKeyValue.setTooltip(null);
                }
               
                request.addAuthParam("keyName", apiKeyName.getText());
                request.addAuthParam("keyValue", apiKeyValue.getText());
                request.addAuthParam("keyLocation", apiKeyLocation.getValue());
                break;
               
            case OAUTH2:
                if (oauth2Token.getText().isEmpty()) {
                    oauth2Token.setStyle("-fx-border-color: #dc3545;");
                    oauth2Token.setTooltip(new Tooltip("Access token cannot be empty"));
                } else {
                    oauth2Token.setStyle("");
                    oauth2Token.setTooltip(null);
                }
               
                request.addAuthParam("accessToken", oauth2Token.getText());
                break;
        }
       
        return request;
    }
   
    /**
     * Synchronize query parameters from URL to the parameters table
     */
    private void syncQueryParamsFromUrl() {
        String url = urlField.getText();
        if (url == null || url.isEmpty() || !url.contains("?")) {
            return;
        }
       
        try {
            // Extract the query part of the URL
            String queryPart = url.substring(url.indexOf("?") + 1);
           
            // Split into individual parameters
            String[] queryParams = queryPart.split("&");
           
            // Create a map of existing parameters for quick lookup
            Map<String, ParamRow> existingParams = new HashMap<>();
            for (ParamRow param : params) {
                existingParams.put(param.getName(), param);
            }
           
            // Process each query parameter
            for (String queryParam : queryParams) {
                if (queryParam.isEmpty()) continue;
               
                String[] parts = queryParam.split("=", 2);
                String name = parts[0];
                String value = parts.length > 1 ? parts[1] : "";
               
                if (existingParams.containsKey(name)) {
                    // Update existing parameter
                    ParamRow existingParam = existingParams.get(name);
                    existingParam.setValue(value);
                    existingParam.setEnabled(true);
                } else {
                    // Add new parameter
                    params.add(new ParamRow(name, value));
                }
            }
        } catch (Exception e) {
            // Silently handle parsing errors
            System.err.println("Error parsing URL query parameters: " + e.getMessage());
        }
    }
   
    /**
     * Build URL with query parameters from the parameters table
     */
    private String buildUrlWithParams() {
        String baseUrl = urlField.getText();
       
        // Remove existing query parameters from the URL
        if (baseUrl.contains("?")) {
            baseUrl = baseUrl.substring(0, baseUrl.indexOf("?"));
        }
       
        StringBuilder urlBuilder = new StringBuilder(baseUrl);
        boolean hasQueryParams = false;
       
        for (ParamRow param : params) {
            if (param.isEnabled() && !param.getName().isEmpty()) {
                if (hasQueryParams) {
                    urlBuilder.append("&");
                } else {
                    urlBuilder.append("?");
                    hasQueryParams = true;
                }
                urlBuilder.append(param.getName()).append("=").append(param.getValue());
            }
        }
       
        return urlBuilder.toString();
    }
   
    /**
     * Update the URL field with query parameters from the table
     */
    private void updateUrlFromParams() {
        // Only update if not currently editing the URL field
        if (!urlField.isFocused()) {
            String newUrl = buildUrlWithParams();
            urlField.setText(newUrl);
        }
    }
   
    private void updateAuthPanes(AuthType authType) {
        basicAuthPane.setVisible(false);
        basicAuthPane.setManaged(false);
        bearerAuthPane.setVisible(false);
        bearerAuthPane.setManaged(false);
        apiKeyAuthPane.setVisible(false);
        apiKeyAuthPane.setManaged(false);
        oauth2AuthPane.setVisible(false);
        oauth2AuthPane.setManaged(false);
       
        switch (authType) {
            case BASIC:
                basicAuthPane.setVisible(true);
                basicAuthPane.setManaged(true);
                break;
            case BEARER:
                bearerAuthPane.setVisible(true);
                bearerAuthPane.setManaged(true);
                break;
            case API_KEY:
                apiKeyAuthPane.setVisible(true);
                apiKeyAuthPane.setManaged(true);
                break;
            case OAUTH2:
                oauth2AuthPane.setVisible(true);
                oauth2AuthPane.setManaged(true);
                break;
        }
    }
   
    /**
     * Loads an API configuration into the request tab.
     *
     * @param config the API configuration to load
     */
    public void loadConfiguration(ApiConfiguration config) {
        urlField.setText(config.getUrl());
        methodComboBox.setValue(config.getMethod());
        bodyTextArea.setText(config.getBody());
       
        // Set headers
        headers.clear();
        for (Map.Entry<String, String> entry : config.getHeaders().entrySet()) {
            headers.add(new HeaderRow(entry.getKey(), entry.getValue()));
        }
       
        // Set auth
        authTypeComboBox.setValue(config.getAuthType());
        updateAuthPanes(config.getAuthType());
       
        switch (config.getAuthType()) {
            case BASIC:
                basicAuthUsername.setText(config.getAuthParams().getOrDefault("username", ""));
                basicAuthPassword.setText(config.getAuthParams().getOrDefault("password", ""));
                break;
            case BEARER:
                bearerToken.setText(config.getAuthParams().getOrDefault("token", ""));
                break;
            case API_KEY:
                apiKeyName.setText(config.getAuthParams().getOrDefault("keyName", ""));
                apiKeyValue.setText(config.getAuthParams().getOrDefault("keyValue", ""));
                apiKeyLocation.setValue(config.getAuthParams().getOrDefault("keyLocation", "header"));
                break;
            case OAUTH2:
                oauth2Token.setText(config.getAuthParams().getOrDefault("accessToken", ""));
                break;
        }
       
        // Sync query parameters from URL
        params.clear();
        syncQueryParamsFromUrl();
    }
   
    /**
     * Loads a request from history into the request tab.
     *
     * @param historyEntry the history entry to load
     */
    public void loadFromHistory(RequestHistoryEntry historyEntry) {
        HttpRequest request = historyEntry.getRequest();
       
        urlField.setText(request.getUrl());
        methodComboBox.setValue(request.getMethod());
        bodyTextArea.setText(request.getBody());
       
        // Set headers
        headers.clear();
        for (Map.Entry<String, String> entry : request.getHeaders().entrySet()) {
            headers.add(new HeaderRow(entry.getKey(), entry.getValue()));
        }
       
        // Set auth
        authTypeComboBox.setValue(request.getAuthType());
        updateAuthPanes(request.getAuthType());
       
        switch (request.getAuthType()) {
            case BASIC:
                basicAuthUsername.setText(request.getAuthParams().getOrDefault("username", ""));
                basicAuthPassword.setText(request.getAuthParams().getOrDefault("password", ""));
                break;
            case BEARER:
                bearerToken.setText(request.getAuthParams().getOrDefault("token", ""));
                break;
            case API_KEY:
                apiKeyName.setText(request.getAuthParams().getOrDefault("keyName", ""));
                apiKeyValue.setText(request.getAuthParams().getOrDefault("keyValue", ""));
                apiKeyLocation.setValue(request.getAuthParams().getOrDefault("keyLocation", "header"));
                break;
            case OAUTH2:
                oauth2Token.setText(request.getAuthParams().getOrDefault("accessToken", ""));
                break;
        }
       
        // Sync query parameters from URL
        params.clear();
        syncQueryParamsFromUrl();
    }
   
    private void logRequest(HttpRequest request) {
        requestLogs.append("=== REQUEST ===\n");
        requestLogs.append(request.getMethod()).append(" ").append(request.getUrl()).append("\n");
        requestLogs.append("--- Headers ---\n");
        for (Map.Entry<String, String> header : request.getHeaders().entrySet()) {
            requestLogs.append(header.getKey()).append(": ").append(header.getValue()).append("\n");
        }
       
        if (request.getBody() != null && !request.getBody().isEmpty()) {
            requestLogs.append("--- Body ---\n");
            requestLogs.append(request.getBody()).append("\n");
        }
       
        requestLogs.append("\n");
    }
   
    private void logResponse(HttpResponse response) {
        requestLogs.append("=== RESPONSE ===\n");
        requestLogs.append("Status: ").append(response.getStatusCode()).append("\n");
        requestLogs.append("Time: ").append(response.getResponseTimeMs()).append(" ms\n");
        requestLogs.append("--- Headers ---\n");
        for (Map.Entry<String, String> header : response.getHeaders().entrySet()) {
            requestLogs.append(header.getKey()).append(": ").append(header.getValue()).append("\n");
        }
       
        if (response.getBody() != null && !response.getBody().isEmpty()) {
            requestLogs.append("--- Body ---\n");
            requestLogs.append(response.getBody()).append("\n");
        }
       
        requestLogs.append("\n");
    }
   
    private void logError(Throwable error) {
        requestLogs.append("=== ERROR ===\n");
        requestLogs.append(error.getMessage()).append("\n");
        for (StackTraceElement element : error.getStackTrace()) {
            requestLogs.append("\tat ").append(element.toString()).append("\n");
        }
        requestLogs.append("\n");
    }
   
    /**
     * Gets the accumulated request logs for this tab.
     *
     * @return the request logs as a string
     */
    public String getRequestLogs() {
        return requestLogs.toString();
    }
   
    /**
     * Gets the current HTTP request configuration.
     *
     * @return the built HTTP request
     */
    public HttpRequest getRequest() {
        return buildRequest();
    }
   
    /**
     * Sets the parent controller reference.
     *
     * @param controller the parent REST client controller
     */
    public void setParentController(RestClientController controller) {
        this.parentController = controller;
    }
   
    private void showLoading(boolean loading) {
        if (loadingContainer != null) {
            loadingContainer.setVisible(loading);
            loadingContainer.setManaged(loading);
        }
        sendButton.setDisable(loading);
    }
   
    @FXML
    private void handleImportCurl() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("curl-import-dialog.fxml"));
            Parent root = loader.load();
           
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Import cURL Command");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
           
            Scene scene = new Scene(root);
            scene.getStylesheets().addAll(
                BootstrapFX.bootstrapFXStylesheet(),
                getClass().getResource("styles.css").toExternalForm()
            );
           
            dialogStage.setScene(scene);
           
            CurlImportDialogController controller = loader.getController();
            dialogStage.showAndWait();
           
            if (controller.isImportSuccessful()) {
                HttpRequest importedRequest = controller.getImportedRequest();
                if (importedRequest != null) {
                    // Validate the imported request
                    List<ValidationUtils.ValidationResult> validationResults = ValidationUtils.validateRequest(importedRequest);
                    boolean hasErrors = false;
                    StringBuilder errorMessage = new StringBuilder("The imported cURL command has validation issues:\n");
                   
                    for (ValidationUtils.ValidationResult result : validationResults) {
                        if (!result.isValid()) {
                            hasErrors = true;
                            errorMessage.append("• ").append(result.getMessage()).append("\n");
                        }
                    }
                   
                    if (hasErrors) {
                        // Show warning but still allow import
                        Alert alert = new Alert(Alert.AlertType.WARNING);
                        alert.setTitle("Import Warning");
                        alert.setHeaderText("The imported cURL command has validation issues");
                        alert.setContentText(errorMessage.toString() + "\n\nDo you want to continue with the import?");
                       
                        // Add Bootstrap styling
                        DialogPane dialogPane = alert.getDialogPane();
                        dialogPane.getStylesheets().add(BootstrapFX.bootstrapFXStylesheet());
                        dialogPane.getStyleClass().addAll("alert", "alert-warning");
                       
                        ButtonType continueButton = new ButtonType("Continue Import");
                        ButtonType cancelButton = ButtonType.CANCEL;
                        alert.getButtonTypes().setAll(continueButton, cancelButton);
                       
                        Optional<ButtonType> result = alert.showAndWait();
                        if (result.isPresent() && result.get() != continueButton) {
                            return;
                        }
                    }
                   
                    // Update the UI with the imported request
                    urlField.setText(importedRequest.getUrl());
                    methodComboBox.setValue(importedRequest.getMethod());
                   
                    // Clear existing headers and add imported ones
                    headers.clear();
                    for (Map.Entry<String, String> header : importedRequest.getHeaders().entrySet()) {
                        headers.add(new HeaderRow(header.getKey(), header.getValue()));
                    }
                   
                    // Set body if present
                    if (importedRequest.getBody() != null && !importedRequest.getBody().isEmpty()) {
                        bodyTextArea.setText(importedRequest.getBody());
                       
                        // Try to detect content type
                        String contentType = importedRequest.getHeaders().get("Content-Type");
                        if (contentType != null) {
                            bodyTypeComboBox.setValue(contentType);
                        }
                    }
                   
                    // Sync query parameters from URL
                    params.clear();
                    syncQueryParamsFromUrl();
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to import cURL command", e);
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Import Error");
            alert.setHeaderText(null);
            alert.setContentText("Failed to import cURL command: " + e.getMessage());
           
            // Add Bootstrap styling
            DialogPane dialogPane = alert.getDialogPane();
            dialogPane.getStylesheets().add(BootstrapFX.bootstrapFXStylesheet());
            dialogPane.getStyleClass().addAll("alert", "alert-danger");
           
            alert.showAndWait();
        }
    }
   
    private RestClientController getParentController() {
        if (parentController != null) {
            return parentController;
        }
       
        try {
            if (urlField.getScene() != null && urlField.getScene().getRoot() != null) {
                RestClientController controller = (RestClientController) urlField.getScene().getRoot().getProperties().get("controller");
                if (controller != null) {
                    this.parentController = controller;
                    return controller;
                }
            }
           
            // Try to find the controller in the parent nodes
            if (urlField.getParent() != null) {
                Parent parent = urlField.getParent();
                while (parent != null) {
                    if (parent.getProperties().containsKey("controller")) {
                        RestClientController controller = (RestClientController) parent.getProperties().get("controller");
                        this.parentController = controller;
                        return controller;
                    }
                    if (parent.getParent() instanceof Parent) {
                        parent = (Parent) parent.getParent();
                    } else {
                        break;
                    }
                }
            }
           
            return null;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to get parent controller", e);
            return null;
        }
    }
   
    public static class HeaderRow {
        private final SimpleStringProperty name;
        private final SimpleStringProperty value;
        private final SimpleBooleanProperty enabled;
       
        public HeaderRow(String name, String value) {
            this.name = new SimpleStringProperty(name);
            this.value = new SimpleStringProperty(value);
            this.enabled = new SimpleBooleanProperty(true);
        }
       
        public String getName() {
            return name.get();
        }
       
        public void setName(String name) {
            this.name.set(name);
        }
       
        public SimpleStringProperty nameProperty() {
            return name;
        }
       
        public String getValue() {
            return value.get();
        }
       
        public void setValue(String value) {
            this.value.set(value);
        }
       
        public SimpleStringProperty valueProperty() {
            return value;
        }
       
        public boolean isEnabled() {
            return enabled.get();
        }
       
        public void setEnabled(boolean enabled) {
            this.enabled.set(enabled);
        }
       
        public SimpleBooleanProperty enabledProperty() {
            return enabled;
        }
    }
   
    public class ParamRow {
        private final SimpleStringProperty name;
        private final SimpleStringProperty value;
        private final SimpleBooleanProperty enabled;
       
        public ParamRow(String name, String value) {
            this.name = new SimpleStringProperty(name);
            this.value = new SimpleStringProperty(value);
            this.enabled = new SimpleBooleanProperty(true);
           
            // Add listener to update URL when enabled state changes
            this.enabled.addListener((obs, oldVal, newVal) -> {
                updateUrlFromParams();
            });
        }
       
        public String getName() {
            return name.get();
        }
       
        public void setName(String name) {
            this.name.set(name);
        }
       
        public SimpleStringProperty nameProperty() {
            return name;
        }
       
        public String getValue() {
            return value.get();
        }
       
        public void setValue(String value) {
            this.value.set(value);
        }
       
        public SimpleStringProperty valueProperty() {
            return value;
        }
       
        public boolean isEnabled() {
            return enabled.get();
        }
       
        public void setEnabled(boolean enabled) {
            this.enabled.set(enabled);
        }
       
        public SimpleBooleanProperty enabledProperty() {
            return enabled;
        }
    }
}
