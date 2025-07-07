package com.rct.restclienttool;

import com.rct.restclienttool.model.*;
import com.rct.restclienttool.service.ApiConfigurationService;
import com.rct.restclienttool.service.RequestHistoryService;
import com.rct.restclienttool.util.JsonFormatter;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
* Main controller for the REST Client application.
* Manages the main UI, request tabs, response display, and configuration operations.
*
 * @author REST Client Tool
* @version 1.0
* @since 1.0
*/
public class RestClientController {
    @FXML private TabPane requestTabPane;
    @FXML private Label statusLabel;
    @FXML private Label timeLabel;
    @FXML private TextArea responseBodyArea;
    @FXML private TableView<HeaderRow> responseHeadersTable;
    @FXML private TableColumn<HeaderRow, String> responseHeaderNameColumn;
    @FXML private TableColumn<HeaderRow, String> responseHeaderValueColumn;
    @FXML private Button prettyPrintButton;
    @FXML private Button rawButton;
    @FXML private Button saveConfigButton;
    @FXML private Button loadConfigButton;
    @FXML private Button deleteConfigButton;
    @FXML private Button historyButton;
    @FXML private Button logsButton;
    @FXML private Button bulkRequestButton;

    private final ApiConfigurationService configService = new ApiConfigurationService();
    private final RequestHistoryService historyService = new RequestHistoryService();
    private final ObservableList<HeaderRow> responseHeaders = FXCollections.observableArrayList();
    private String rawResponseBody;

    @FXML private BorderPane rootPane;
   
    /**
     * Initializes the controller and sets up UI components.
     * Called automatically by JavaFX after FXML loading.
     */
    @FXML
    public void initialize() {
        // Store controller reference in scene properties
        responseHeaderNameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        responseHeaderValueColumn.setCellValueFactory(cellData -> cellData.getValue().valueProperty());
        responseHeadersTable.setItems(responseHeaders);
       
        // Store this controller in the root pane properties
        rootPane.getProperties().put("controller", this);
       
        // Add a new tab button
        Tab addTab = new Tab("+");
        addTab.setClosable(false);
        requestTabPane.getTabs().add(addTab);
       
        requestTabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldTab, newTab) -> {
            if (newTab == addTab) {
                // Create a new tab
                try {
                    Tab tab = new Tab("New Request");
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("request-tab-content.fxml"));
                    tab.setContent(loader.load());
                   
                    // Store reference to the controller
                    RequestTabController tabController = loader.getController();
                    tab.getContent().getProperties().put("controller", tabController);
                   
                    // Add the new tab before the "+" tab
                    requestTabPane.getTabs().add(requestTabPane.getTabs().size() - 1, tab);
                    requestTabPane.getSelectionModel().select(tab);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
       
        // Initialize the first tab's controller
        if (!requestTabPane.getTabs().isEmpty()) {
            Tab firstTab = requestTabPane.getTabs().get(0);
            if (firstTab.getContent() != null) {
                RequestTabController controller = (RequestTabController) firstTab.getContent().getProperties().get("controller");
                if (controller != null) {
                    controller.setParentController(this);
               }
            }
        }
    }
   
    /**
     * Sets this controller as the root controller in the scene properties.
     * Called from the application class during startup.
     */
    public void setAsRootController() {
        // This method is called from the application class
        if (rootPane != null) {
            rootPane.getProperties().put("controller", this);
        }
    }

    @FXML
    private void handlePrettyPrint() {
        if (rawResponseBody != null && JsonFormatter.isValidJson(rawResponseBody)) {
            responseBodyArea.setText(JsonFormatter.prettyPrint(rawResponseBody));
        }
    }

    @FXML
    private void handleRawView() {
        if (rawResponseBody != null) {
            responseBodyArea.setText(rawResponseBody);
        }
    }

    @FXML
    private void handleSaveConfig() {
        Tab selectedTab = requestTabPane.getSelectionModel().getSelectedItem();
        if (selectedTab != null && selectedTab.getContent() != null) {
            RequestTabController controller = getControllerFromTab(selectedTab);
            if (controller != null) {
               HttpRequest request = controller.getRequest();
               
                // Validate request before saving
                if (request.getUrl() == null || request.getUrl().trim().isEmpty()) {
                    showAlert(Alert.AlertType.WARNING, "Validation Error", "URL is required to save configuration.");
                    return;
                }
               
                TextInputDialog dialog = new TextInputDialog();
                dialog.setTitle("Save Configuration");
                dialog.setHeaderText("Enter a name for this configuration");
                dialog.setContentText("Name:");
               
                Optional<String> result = dialog.showAndWait();
                result.ifPresent(name -> {
                    // Validate configuration name
                    if (name.trim().isEmpty()) {
                        showAlert(Alert.AlertType.WARNING, "Validation Error", "Configuration name cannot be empty.");
                        return;
                    }
                   
                    if (name.length() > 50) {
                        showAlert(Alert.AlertType.WARNING, "Validation Error", "Configuration name must be 50 characters or less.");
                        return;
                    }
                   
                    ApiConfiguration config = new ApiConfiguration();
                    config.setName(name.trim());
                    config.setUrl(request.getUrl());
                    config.setMethod(request.getMethod());
                    config.setHeaders(request.getHeaders());
                    config.setBody(request.getBody());
                    config.setAuthType(request.getAuthType());
                    config.setAuthParams(request.getAuthParams());
                   
                    configService.saveConfiguration(config);
                    showAlert(Alert.AlertType.INFORMATION, "Configuration Saved",
                            "The API configuration has been saved successfully.");
                });
            }
        }
    }

    @FXML
    private void handleLoadConfig() {
        List<ApiConfiguration> configs = configService.loadAllConfigurations();
        if (configs.isEmpty()) {
            showAlert(Alert.AlertType.INFORMATION, "No Configurations",
                    "There are no saved API configurations.");
            return;
        }
       
        ChoiceDialog<ApiConfiguration> dialog = new ChoiceDialog<>(configs.get(0), configs);
        dialog.setTitle("Load Configuration");
        dialog.setHeaderText("Select a configuration to load");
        dialog.setContentText("Configuration:");
       
        Optional<ApiConfiguration> result = dialog.showAndWait();
        result.ifPresent(config -> {
            Tab selectedTab = requestTabPane.getSelectionModel().getSelectedItem();
            if (selectedTab != null && selectedTab.getContent() != null) {
                RequestTabController controller = getControllerFromTab(selectedTab);
                if (controller != null) {
                    controller.loadConfiguration(config);
                }
            }
        });
    }

    @FXML
    private void handleDeleteConfig() {
        List<ApiConfiguration> configs = configService.loadAllConfigurations();
        if (configs.isEmpty()) {
            showAlert(Alert.AlertType.INFORMATION, "No Configurations",
                    "There are no saved API configurations to delete.");
            return;
        }
       
        ChoiceDialog<ApiConfiguration> dialog = new ChoiceDialog<>(configs.get(0), configs);
        dialog.setTitle("Delete Configuration");
        dialog.setHeaderText("Select a configuration to delete");
        dialog.setContentText("Configuration:");
       
        Optional<ApiConfiguration> result = dialog.showAndWait();
        result.ifPresent(config -> {
            Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
            confirmDialog.setTitle("Confirm Delete");
            confirmDialog.setHeaderText("Delete Configuration");
            confirmDialog.setContentText("Are you sure you want to delete '" + config.getName() + "'?");
           
            Optional<ButtonType> confirmResult = confirmDialog.showAndWait();
            if (confirmResult.isPresent() && confirmResult.get() == ButtonType.OK) {
                configService.deleteConfiguration(config.getName());
                showAlert(Alert.AlertType.INFORMATION, "Configuration Deleted",
                        "The configuration has been deleted successfully.");
            }
        });
    }

    @FXML
    private void handleShowHistory() {
        try {
            List<RequestHistoryEntry> history = historyService.loadHistory();
           
            Stage historyStage = new Stage();
            historyStage.setTitle("Request History");
            historyStage.initModality(Modality.APPLICATION_MODAL);
           
            ListView<RequestHistoryEntry> listView = new ListView<>();
            listView.setItems(FXCollections.observableArrayList(history));
           
            // Custom cell factory for better display using BootstrapFX
            listView.setCellFactory(lv -> new ListCell<RequestHistoryEntry>() {
                @Override
                protected void updateItem(RequestHistoryEntry item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        // Format the timestamp
                        String timestamp = item.getTimestamp().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                       
                        // Create styled text
                        HBox container = new HBox(10);
                       
                        // Method label with color
                        Label methodLabel = new Label(item.getMethod());
                        methodLabel.getStyleClass().add("label");
                       
                        // Apply Bootstrap colors based on method
                        switch (item.getMethod()) {
                            case "GET":
                                methodLabel.getStyleClass().add("label-primary");
                                break;
                            case "POST":
                                methodLabel.getStyleClass().add("label-success");
                                break;
                            case "PUT":
                                methodLabel.getStyleClass().add("label-warning");
                                break;
                            case "DELETE":
                                methodLabel.getStyleClass().add("label-danger");
                                break;
                            default:
                                methodLabel.getStyleClass().add("label-info");
                                break;
                        }
                       
                        // Status code with color
                        Label statusLabel = new Label(String.valueOf(item.getStatusCode()));
                        statusLabel.getStyleClass().add("label");
                       
                        if (item.getStatusCode() >= 200 && item.getStatusCode() < 300) {
                            statusLabel.getStyleClass().add("label-success");
                        } else if (item.getStatusCode() >= 300 && item.getStatusCode() < 400) {
                            statusLabel.getStyleClass().add("label-warning");
                        } else if (item.getStatusCode() >= 400) {
                            statusLabel.getStyleClass().add("label-danger");
                        }
                       
                        // URL and timestamp
                        VBox details = new VBox(2);
                        Label urlLabel = new Label(item.getUrl());
                        urlLabel.getStyleClass().add("lead");
                        Label timeLabel = new Label(timestamp);
                        timeLabel.getStyleClass().add("text-muted");
                        details.getChildren().addAll(urlLabel, timeLabel);
                       
                        container.getChildren().addAll(methodLabel, details, statusLabel);
                        setGraphic(container);
                    }
                }
            });
           
            Button loadButton = new Button("Load Selected Request");
            loadButton.getStyleClass().addAll("btn", "btn-primary");
            loadButton.setOnAction(event -> {
                RequestHistoryEntry selected = listView.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    historyStage.close();
                   
                    // Create a new tab with the historical request
                    try {
                        Tab tab = new Tab(selected.getMethod() + " " + selected.getUrl());
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("request-tab-content.fxml"));
                        tab.setContent(loader.load());
                        RequestTabController controller = loader.getController();
                        controller.loadFromHistory(selected);
                        controller.setParentController(this);
                       
                        // Add the new tab before the "+" tab
                        requestTabPane.getTabs().add(requestTabPane.getTabs().size() - 1, tab);
                        requestTabPane.getSelectionModel().select(tab);
                       
                        // Display the response
                        updateResponseView(selected.getResponse());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
           
            Button clearButton = new Button("Clear History");
            clearButton.getStyleClass().addAll("btn", "btn-danger");
            clearButton.setOnAction(event -> {
                historyService.clearHistory();
                listView.getItems().clear();
            });
           
            Button closeButton = new Button("Close");
            closeButton.getStyleClass().addAll("btn", "btn-default");
            closeButton.setOnAction(event -> historyStage.close());
           
            HBox buttonBar = new HBox(10);
            buttonBar.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
            buttonBar.getChildren().addAll(loadButton, clearButton, closeButton);
           
            Label titleLabel = new Label("Request History");
            titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
           
            VBox root = new VBox(10, titleLabel, listView, buttonBar);
            root.setPadding(new javafx.geometry.Insets(15));
            root.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());
           
            Scene scene = new Scene(root, 700, 500);
            historyStage.setScene(scene);
            historyStage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load request history.");
        }
    }

    @FXML
    private void handleBulkRequests() {
        try {
            Stage bulkStage = new Stage();
            bulkStage.setTitle("Bulk Request Execution");
            bulkStage.initModality(Modality.APPLICATION_MODAL);
           
            FXMLLoader loader = new FXMLLoader(getClass().getResource("bulk-request-view.fxml"));
            Scene scene = new Scene(loader.load(), 1000, 800);
            scene.getStylesheets().addAll(
                org.kordamp.bootstrapfx.BootstrapFX.bootstrapFXStylesheet(),
                getClass().getResource("styles.css").toExternalForm()
            );
           
            bulkStage.setScene(scene);
            bulkStage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to open bulk request window: " + e.getMessage());
        }
    }

    @FXML
    private void handleViewLogs() {
        try {
            Stage logsStage = new Stage();
            logsStage.setTitle("Request Logs");
            logsStage.initModality(Modality.APPLICATION_MODAL);
           
            TextArea logsArea = new TextArea();
            logsArea.setEditable(false);
            logsArea.setWrapText(true);
            logsArea.getStyleClass().add("code-area");
           
            // Get the currently selected tab's controller
            Tab selectedTab = requestTabPane.getSelectionModel().getSelectedItem();
            if (selectedTab != null && selectedTab.getContent() != null) {
                RequestTabController controller = getControllerFromTab(selectedTab);
                if (controller != null) {
                    String logs = controller.getRequestLogs();
                    logsArea.setText(logs);
                }
            }
           
            Button closeButton = new Button("Close");
            closeButton.getStyleClass().addAll("btn", "btn-primary");
            closeButton.setOnAction(e -> logsStage.close());
           
            HBox buttonBar = new HBox(10);
            buttonBar.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
            buttonBar.getChildren().add(closeButton);
           
            Label titleLabel = new Label("Request and Response Logs");
            titleLabel.getStyleClass().addAll("h4");
           
            VBox root = new VBox(10, titleLabel, logsArea, buttonBar);
            root.setPadding(new javafx.geometry.Insets(15));
            root.getStyleClass().add("panel-default");
           
            Scene scene = new Scene(root, 800, 600);
            logsStage.setScene(scene);
            logsStage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to display logs.");
        }
    }

    /**
     * Updates the response view with the given HTTP response.
     *
     * @param response the HTTP response to display
     */
    public void updateResponseView(HttpResponse response) {
        if (response == null) {
            statusLabel.setText("No response");
            timeLabel.setText("");
            responseBodyArea.setText("");
            responseHeaders.clear();
            rawResponseBody = null;
            return;
        }
       
        // Update status
        int statusCode = response.getStatusCode();
        statusLabel.setText(String.valueOf(statusCode));
       
        // Remove all status classes
        statusLabel.getStyleClass().removeAll(
            "badge-success", "badge-warning", "badge-danger",
            "badge-info", "badge-primary", "badge-secondary",
            "status-200", "status-201", "status-204",
            "status-301", "status-302", "status-303", "status-307", "status-308",
            "status-400", "status-401", "status-403", "status-404", "status-429",
            "status-500", "status-502", "status-503", "status-504"
        );
       
        // Reset any inline styles
        statusLabel.setStyle("");
       
        // Add specific status code class if available
        String specificStatusClass = "status-" + statusCode;
        statusLabel.getStyleClass().add(specificStatusClass);
       
        // Also add range-based class as fallback
        if (statusCode >= 200 && statusCode < 300) {
            statusLabel.getStyleClass().add("badge-success");
        } else if (statusCode >= 300 && statusCode < 400) {
            statusLabel.getStyleClass().add("badge-warning");
        } else if (statusCode >= 400 && statusCode < 500) {
            statusLabel.getStyleClass().add("badge-danger");
        } else if (statusCode >= 500) {
            statusLabel.getStyleClass().add("badge-danger");
        }
       
        // Update time
        timeLabel.setText(response.getResponseTimeMs() + " ms");
       
        // Update body
        rawResponseBody = response.getBody();
        if (rawResponseBody != null && JsonFormatter.isValidJson(rawResponseBody)) {
            responseBodyArea.setText(JsonFormatter.prettyPrint(rawResponseBody));
        } else {
            responseBodyArea.setText(rawResponseBody);
        }
       
        // Update headers
        responseHeaders.clear();
        for (Map.Entry<String, String> entry : response.getHeaders().entrySet()) {
            responseHeaders.add(new HeaderRow(entry.getKey(), entry.getValue()));
        }
    }

    private RequestTabController getControllerFromTab(Tab tab) {
        try {
            if (tab != null && tab.getContent() != null) {
                return (RequestTabController) tab.getContent().getProperties().get("controller");
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
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

        public SimpleStringProperty nameProperty() {
            return name;
        }

        public String getValue() {
            return value.get();
        }

        public SimpleStringProperty valueProperty() {
            return value;
        }

        public boolean isEnabled() {
            return enabled.get();
        }

        public SimpleBooleanProperty enabledProperty() {
            return enabled;
        }
    }
}