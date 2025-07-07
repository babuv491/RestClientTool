package com.rct.restclienttool;

import com.rct.restclienttool.model.BulkRequest;
import com.rct.restclienttool.model.BulkResponse;
import com.rct.restclienttool.model.HttpRequest;
import com.rct.restclienttool.model.HttpResponse;
import com.rct.restclienttool.service.BulkRequestService;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.GridPane;
import javafx.geometry.Insets;
import javafx.stage.FileChooser;
import com.rct.restclienttool.service.RequestHistoryService;
import com.rct.restclienttool.model.RequestHistoryEntry;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
* Controller for the bulk request execution window.
* Manages multiple HTTP requests execution with progress tracking and result analysis.
*
 * @author REST Client Tool
* @version 1.0
* @since 1.0
*/
public class BulkRequestController {

    @FXML private TableView<HttpRequest> requestsTable;
    @FXML private TableColumn<HttpRequest, String> methodColumn;
    @FXML private TableColumn<HttpRequest, String> urlColumn;
    @FXML private TableColumn<HttpRequest, String> nameColumn;
   
    @FXML private TableView<HttpResponse> resultsTable;
    @FXML private TableColumn<HttpResponse, String> statusColumn;
    @FXML private TableColumn<HttpResponse, String> timeColumn;
    @FXML private TableColumn<HttpResponse, String> responseColumn;
   
    @FXML private CheckBox runSequentiallyCheckbox;
    @FXML private Spinner<Integer> delaySpinner;
    @FXML private Button executeButton;
    @FXML private Button addRequestButton;
    @FXML private Button removeRequestButton;
    @FXML private Button importRequestsButton;
    @FXML private TextArea summaryArea;
    @FXML private ProgressBar progressBar;
    @FXML private Label statusLabel;
   
    private final BulkRequestService bulkRequestService = new BulkRequestService();
    private final RequestHistoryService historyService = new RequestHistoryService();
    private final ObservableList<HttpRequest> requests = FXCollections.observableArrayList();
    private final ObservableList<HttpResponse> responses = FXCollections.observableArrayList();
   
    /**
     * Initializes the bulk request controller and sets up UI components.
     * Called automatically by JavaFX after FXML loading.
     */
    @FXML
    public void initialize() {
        // Initialize request table
        methodColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getMethod()));
        urlColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getUrl()));
        nameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));
        requestsTable.setItems(requests);
       
        // Initialize results table
        statusColumn.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getStatusCode())));
        timeColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getExecutionTime() + " ms"));
        responseColumn.setCellValueFactory(data -> {
            String body = data.getValue().getBody();
            if (body != null && body.length() > 50) {
                return new SimpleStringProperty(body.substring(0, 50) + "...");
            }
            return new SimpleStringProperty(body);
        });
        resultsTable.setItems(responses);
       
        // Initialize spinner
        SpinnerValueFactory<Integer> valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 10000, 0, 100);
        delaySpinner.setValueFactory(valueFactory);
       
        // Set up button actions
        executeButton.setOnAction(e -> executeBulkRequests());
        addRequestButton.setOnAction(e -> addNewRequest());
        removeRequestButton.setOnAction(e -> removeSelectedRequest());
        importRequestsButton.setOnAction(e -> importRequests());
       
        // Double-click to edit request
        requestsTable.setRowFactory(tv -> {
            TableRow<HttpRequest> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    HttpRequest selectedRequest = row.getItem();
                    if (editRequest(selectedRequest)) {
                        requestsTable.refresh();
                    }
                }
            });
            return row;
        });
       
        // Double-click to view response details
        resultsTable.setRowFactory(tv -> {
            TableRow<HttpResponse> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    showResponseDetails(row.getItem());
                }
            });
            return row;
        });
    }
   
    private void executeBulkRequests() {
        // Validate requests before execution
        String validationError = validateBulkRequests();
        if (validationError != null) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", validationError);
            return;
        }
       
        // Clear previous results
        responses.clear();
        summaryArea.clear();
       
        // Update UI state
        executeButton.setDisable(true);
        progressBar.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
        statusLabel.setText("Executing requests...");
       
        // Create bulk request
        BulkRequest bulkRequest = new BulkRequest(
                new ArrayList<>(requests),
                runSequentiallyCheckbox.isSelected(),
                delaySpinner.getValue()
        );
       
        // Execute in background with progress tracking
        CompletableFuture.supplyAsync(() -> {
            return executeBulkRequestsWithProgress(bulkRequest);
        }).thenAccept(result -> Platform.runLater(() -> {
            // Update UI with results
            responses.addAll(result.getResponses());
            displaySummary(result);
            progressBar.setProgress(1.0);
            statusLabel.setText("Execution completed - " + result.getResponses().size() + " requests");
            executeButton.setDisable(false);
        })).exceptionally(ex -> {
            Platform.runLater(() -> {
                showAlert(Alert.AlertType.ERROR, "Execution Error", ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage());
                progressBar.setProgress(0);
                statusLabel.setText("Execution failed");
                executeButton.setDisable(false);
            });
            return null;
        });
    }
   
    private BulkResponse executeBulkRequestsWithProgress(BulkRequest bulkRequest) {
        List<HttpResponse> responses = new ArrayList<>();
        int totalRequests = bulkRequest.getRequests().size();
        long startTime = System.currentTimeMillis();
       
        for (int i = 0; i < totalRequests; i++) {
            final int currentIndex = i;
            Platform.runLater(() -> {
                double progress = (double) currentIndex / totalRequests;
                progressBar.setProgress(progress);
                statusLabel.setText("Executing request " + (currentIndex + 1) + " of " + totalRequests);
            });
           
            HttpResponse response = bulkRequestService.executeRequest(bulkRequest.getRequests().get(i));
            responses.add(response);
           
            if (bulkRequest.getDelayBetweenRequests() > 0 && i < totalRequests - 1) {
                try {
                    Thread.sleep(bulkRequest.getDelayBetweenRequests());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
       
        long totalTime = System.currentTimeMillis() - startTime;
        BulkResponse bulkResponse = new BulkResponse(responses, totalTime);
        bulkResponse.setSummary(generateSummary(responses));
        return bulkResponse;
    }
   
    private void displaySummary(BulkResponse bulkResponse) {
        StringBuilder summary = new StringBuilder();
        Map<String, Object> summaryData = bulkResponse.getSummary();
       
        summary.append("BULK REQUEST SUMMARY\n");
        summary.append("====================\n\n");
        summary.append("Total execution time: ").append(formatTime(bulkResponse.getTotalExecutionTime())).append("\n");
        summary.append("Total requests: ").append(bulkResponse.getResponses().size()).append("\n");
        summary.append("Successful requests: ").append(summaryData.get("successfulRequests")).append("\n");
        summary.append("Failed requests: ").append(summaryData.get("failedRequests")).append("\n\n");
       
        summary.append("Response times:\n");
        summary.append("  Average: ").append(String.format("%.2f", summaryData.get("averageResponseTime"))).append(" ms\n");
        summary.append("  Min: ").append(summaryData.get("minResponseTime")).append(" ms\n");
        summary.append("  Max: ").append(summaryData.get("maxResponseTime")).append(" ms\n\n");
       
        summary.append("Status code distribution:\n");
        Map<Integer, Long> statusCodes = (Map<Integer, Long>) summaryData.get("statusCodeCounts");
        statusCodes.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    String statusText = getStatusText(entry.getKey());
                    summary.append("  ").append(entry.getKey()).append(" (").append(statusText).append("): ").append(entry.getValue()).append("\n");
                });
       
        // Add throughput calculation
        double throughput = (double) bulkResponse.getResponses().size() / (bulkResponse.getTotalExecutionTime() / 1000.0);
        summary.append("\nThroughput: ").append(String.format("%.2f", throughput)).append(" requests/second\n");
       
        summaryArea.setText(summary.toString());
    }
   
    private String formatTime(long milliseconds) {
        if (milliseconds < 1000) {
            return milliseconds + " ms";
        } else if (milliseconds < 60000) {
            return String.format("%.2f seconds", milliseconds / 1000.0);
        } else {
            long minutes = milliseconds / 60000;
            long seconds = (milliseconds % 60000) / 1000;
            return minutes + "m " + seconds + "s";
        }
    }
   
    private String getStatusText(int statusCode) {
        if (statusCode >= 200 && statusCode < 300) return "Success";
        if (statusCode >= 300 && statusCode < 400) return "Redirect";
        if (statusCode >= 400 && statusCode < 500) return "Client Error";
        if (statusCode >= 500) return "Server Error";
        if (statusCode == -1) return "Network Error";
        return "Unknown";
    }
   
    private Map<String, Object> generateSummary(List<HttpResponse> responses) {
        Map<String, Object> summary = new HashMap<>();
       
        Map<Integer, Long> statusCodeCounts = responses.stream()
                .collect(Collectors.groupingBy(HttpResponse::getStatusCode, Collectors.counting()));
        summary.put("statusCodeCounts", statusCodeCounts);
       
        double avgResponseTime = responses.stream()
                .mapToLong(HttpResponse::getExecutionTime)
               .average()
                .orElse(0);
        summary.put("averageResponseTime", avgResponseTime);
       
        long minResponseTime = responses.stream()
                .mapToLong(HttpResponse::getExecutionTime)
                .min()
                .orElse(0);
        summary.put("minResponseTime", minResponseTime);
       
        long maxResponseTime = responses.stream()
                .mapToLong(HttpResponse::getExecutionTime)
                .max()
                .orElse(0);
        summary.put("maxResponseTime", maxResponseTime);
       
        long successfulRequests = responses.stream()
                .filter(r -> r.getStatusCode() >= 200 && r.getStatusCode() < 300)
                .count();
        summary.put("successfulRequests", successfulRequests);
       
        long failedRequests = responses.size() - successfulRequests;
        summary.put("failedRequests", failedRequests);
       
        return summary;
    }
   
    private void addNewRequest() {
        HttpRequest newRequest = new HttpRequest();
        newRequest.setMethod("GET");
        newRequest.setUrl("https://api.example.com");
        newRequest.setName("New Request " + (requests.size() + 1));
       
        if (editRequest(newRequest)) {
            requests.add(newRequest);
        }
    }
   
    private boolean editRequest(HttpRequest request) {
        Dialog<HttpRequest> dialog = new Dialog<>();
        dialog.setTitle("Edit Request");
        dialog.setHeaderText("Configure HTTP Request");
       
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
       
        TextField nameField = new TextField(request.getName());
        ComboBox<String> methodBox = new ComboBox<>();
        methodBox.getItems().addAll("GET", "POST", "PUT", "DELETE", "PATCH");
        methodBox.setValue(request.getMethod());
        TextField urlField = new TextField(request.getUrl());
        TextArea headersArea = new TextArea();
        TextArea bodyArea = new TextArea();
       
        // Populate headers
        if (request.getHeaders() != null) {
            StringBuilder headerText = new StringBuilder();
            request.getHeaders().forEach((k, v) -> headerText.append(k).append(": ").append(v).append("\n"));
            headersArea.setText(headerText.toString());
        }
       
        bodyArea.setText(request.getBody() != null ? request.getBody() : "");
       
        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Method:"), 0, 1);
        grid.add(methodBox, 1, 1);
        grid.add(new Label("URL:"), 0, 2);
        grid.add(urlField, 1, 2);
        grid.add(new Label("Headers:"), 0, 3);
        grid.add(headersArea, 1, 3);
        grid.add(new Label("Body:"), 0, 4);
        grid.add(bodyArea, 1, 4);
       
        dialog.getDialogPane().setContent(grid);
       
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
       
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                request.setName(nameField.getText());
                request.setMethod(methodBox.getValue());
                request.setUrl(urlField.getText());
                request.setBody(bodyArea.getText());
               
                // Parse headers
                Map<String, String> headers = new HashMap<>();
                String[] headerLines = headersArea.getText().split("\n");
                for (String line : headerLines) {
                    if (line.contains(":")) {
                        String[] parts = line.split(":", 2);
                        headers.put(parts[0].trim(), parts[1].trim());
                    }
                }
                request.setHeaders(headers);
                return request;
            }
            return null;
        });
       
        Optional<HttpRequest> result = dialog.showAndWait();
        return result.isPresent();
    }
   
    private void removeSelectedRequest() {
        HttpRequest selected = requestsTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            requests.remove(selected);
        }
    }
   
    private void importRequests() {
        ChoiceDialog<String> dialog = new ChoiceDialog<>("Sample Requests", "Sample Requests", "From History", "From File");
        dialog.setTitle("Import Requests");
        dialog.setHeaderText("Select import source");
        dialog.setContentText("Import from:");
       
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(choice -> {
            switch (choice) {
                case "Sample Requests":
                    importSampleRequests();
                    break;
                case "From History":
                    importFromHistory();
                    break;
                case "From File":
                    importFromFile();
                    break;
            }
        });
    }
   
    private void importSampleRequests() {
        HttpRequest req1 = new HttpRequest();
        req1.setMethod("GET");
        req1.setUrl("https://jsonplaceholder.typicode.com/posts/1");
        req1.setName("Get Post");
       
        HttpRequest req2 = new HttpRequest();
        req2.setMethod("GET");
        req2.setUrl("https://jsonplaceholder.typicode.com/users/1");
        req2.setName("Get User");
       
        HttpRequest req3 = new HttpRequest();
        req3.setMethod("POST");
        req3.setUrl("https://jsonplaceholder.typicode.com/posts");
        req3.setName("Create Post");
        req3.setBody("{\"title\": \"Test\", \"body\": \"Test body\", \"userId\": 1}");
        req3.getHeaders().put("Content-Type", "application/json");
       
        requests.addAll(req1, req2, req3);
    }
   
    private void importFromHistory() {
        try {
            List<RequestHistoryEntry> history = historyService.loadHistory();
            if (history.isEmpty()) {
                showAlert(Alert.AlertType.INFORMATION, "No History", "No request history found.");
                return;
            }
           
            Dialog<List<RequestHistoryEntry>> dialog = new Dialog<>();
            dialog.setTitle("Import from History");
            dialog.setHeaderText("Select requests to import");
           
            ListView<RequestHistoryEntry> listView = new ListView<>();
            listView.getItems().addAll(history);
            listView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
           
            listView.setCellFactory(lv -> new ListCell<RequestHistoryEntry>() {
                @Override
                protected void updateItem(RequestHistoryEntry item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.getMethod() + " " + item.getUrl() + " (" + item.getStatusCode() + ")");
                    }
                }
            });
           
            dialog.getDialogPane().setContent(listView);
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
           
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == ButtonType.OK) {
                    return listView.getSelectionModel().getSelectedItems();
                }
                return null;
            });
           
            Optional<List<RequestHistoryEntry>> result = dialog.showAndWait();
            result.ifPresent(selectedEntries -> {
                for (RequestHistoryEntry entry : selectedEntries) {
                    HttpRequest request = new HttpRequest();
                    request.setName(entry.getMethod() + " " + entry.getUrl());
                    request.setMethod(entry.getMethod());
                    request.setUrl(entry.getUrl());
                    requests.add(request);
                }
                showAlert(Alert.AlertType.INFORMATION, "Import Complete",
                        "Imported " + selectedEntries.size() + " requests from history.");
            });
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Import Error", "Failed to import from history: " + e.getMessage());
        }
    }
   
    private void importFromFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Import Requests from File");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("JSON Files", "*.json"),
                new FileChooser.ExtensionFilter("Text Files", "*.txt"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );
       
        File selectedFile = fileChooser.showOpenDialog(requestsTable.getScene().getWindow());
        if (selectedFile != null) {
            try {
                String content = Files.readString(selectedFile.toPath());
                List<HttpRequest> importedRequests = parseRequestsFromFile(content, selectedFile.getName());
               
                if (!importedRequests.isEmpty()) {
                    requests.addAll(importedRequests);
                    showAlert(Alert.AlertType.INFORMATION, "Import Complete",
                            "Imported " + importedRequests.size() + " requests from file.");
                } else {
                    showAlert(Alert.AlertType.WARNING, "No Requests Found",
                            "No valid requests found in the selected file.");
                }
            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "File Error", "Failed to read file: " + e.getMessage());
            }
        }
    }
   
    private List<HttpRequest> parseRequestsFromFile(String content, String fileName) {
        List<HttpRequest> requests = new ArrayList<>();
       
        try {
            if (fileName.toLowerCase().endsWith(".json")) {
                requests.addAll(parseJsonRequests(content));
            } else {
                requests.addAll(parseTextRequests(content));
            }
        } catch (Exception e) {
            System.err.println("Error parsing file: " + e.getMessage());
        }
       
        return requests;
    }
   
    private List<HttpRequest> parseJsonRequests(String jsonContent) {
        List<HttpRequest> requests = new ArrayList<>();
       
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(jsonContent);
           
            if (rootNode.isArray()) {
                for (JsonNode requestNode : rootNode) {
                    HttpRequest request = parseJsonRequest(requestNode);
                    if (request != null) {
                        requests.add(request);
                    }
                }
            } else {
                HttpRequest request = parseJsonRequest(rootNode);
                if (request != null) {
                    requests.add(request);
                }
            }
        } catch (Exception e) {
            System.err.println("Error parsing JSON: " + e.getMessage());
        }
       
        return requests;
    }
   
    private HttpRequest parseJsonRequest(JsonNode node) {
        try {
            HttpRequest request = new HttpRequest();
           
            if (node.has("name")) {
                request.setName(node.get("name").asText());
            }
            if (node.has("method")) {
                request.setMethod(node.get("method").asText());
            }
            if (node.has("url")) {
                request.setUrl(node.get("url").asText());
            }
            if (node.has("body")) {
                request.setBody(node.get("body").asText());
            }
           
            // Parse headers
            if (node.has("headers")) {
                JsonNode headersNode = node.get("headers");
                Map<String, String> headers = new HashMap<>();
                headersNode.fields().forEachRemaining(entry ->
                    headers.put(entry.getKey(), entry.getValue().asText())
                );
                request.setHeaders(headers);
            }
           
            return validateRequest(request) ? request : null;
        } catch (Exception e) {
            return null;
        }
    }
   
    private List<HttpRequest> parseTextRequests(String textContent) {
        List<HttpRequest> requests = new ArrayList<>();
        String[] lines = textContent.split("\\n");
       
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) continue;
           
            // Simple format: METHOD URL [NAME]
            String[] parts = line.split("\\s+", 3);
            if (parts.length >= 2) {
                HttpRequest request = new HttpRequest();
                request.setMethod(parts[0].toUpperCase());
                request.setUrl(parts[1]);
                request.setName(parts.length > 2 ? parts[2] : parts[0] + " " + parts[1]);
               
                if (validateRequest(request)) {
                    requests.add(request);
                }
            }
        }
       
        return requests;
    }
   
    private void showResponseDetails(HttpResponse response) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Response Details");
        dialog.setHeaderText("Response Status: " + response.getStatusCode());
       
        TabPane tabPane = new TabPane();
       
        // Body tab
        Tab bodyTab = new Tab("Body");
        TextArea bodyArea = new TextArea(response.getBody());
        bodyArea.setEditable(false);
        bodyArea.setWrapText(true);
        bodyArea.setPrefWidth(600);
        bodyArea.setPrefHeight(400);
        bodyTab.setContent(bodyArea);
       
        // Headers tab
        Tab headersTab = new Tab("Headers");
        VBox headersBox = new VBox(5);
        if (response.getHeaders() != null) {
            response.getHeaders().forEach((key, value) -> {
                Label headerLabel = new Label(key + ": " + value);
                headersBox.getChildren().add(headerLabel);
            });
        }
        ScrollPane scrollPane = new ScrollPane(headersBox);
        scrollPane.setFitToWidth(true);
        headersTab.setContent(scrollPane);
       
        tabPane.getTabs().addAll(bodyTab, headersTab);
        dialog.getDialogPane().setContent(tabPane);
       
        ButtonType closeButton = new ButtonType("Close", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().add(closeButton);
       
        dialog.showAndWait();
    }
   
    private String validateBulkRequests() {
        if (requests.isEmpty()) {
            return "Please add at least one request to execute.";
        }
       
        for (int i = 0; i < requests.size(); i++) {
            HttpRequest request = requests.get(i);
            String error = validateSingleRequest(request, i + 1);
            if (error != null) {
                return error;
            }
        }
       
        return null;
    }
   
    private String validateSingleRequest(HttpRequest request, int index) {
        if (request.getUrl() == null || request.getUrl().trim().isEmpty()) {
            return "Request #" + index + ": URL is required.";
        }
       
        if (!isValidUrl(request.getUrl())) {
            return "Request #" + index + ": Invalid URL format.";
        }
       
        if (request.getMethod() == null || request.getMethod().trim().isEmpty()) {
            return "Request #" + index + ": HTTP method is required.";
        }
        
        String[] validMethods = {"GET", "POST", "PUT", "DELETE", "PATCH", "HEAD", "OPTIONS"};
        boolean validMethod = false;
        for (String method : validMethods) {
            if (method.equalsIgnoreCase(request.getMethod().trim())) {
                validMethod = true;
                break;
            }
        }
       
        if (!validMethod) {
            return "Request #" + index + ": Invalid HTTP method '" + request.getMethod() + "'.";
        }
       
        return null;
    }
   
    private boolean validateRequest(HttpRequest request) {
        return request != null &&
               request.getUrl() != null && !request.getUrl().trim().isEmpty() &&
               request.getMethod() != null && !request.getMethod().trim().isEmpty() &&
               isValidUrl(request.getUrl());
    }
   
    private boolean isValidUrl(String url) {
        try {
            new java.net.URL(url.trim());
            return true;
        } catch (Exception e) {
            return false;
        }
    }
   
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}