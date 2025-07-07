# REST Client Tool - Developer Guide

## Project Structure

```
src/main/java/com/rct/restclienttool/
├── model/              # Data models
├── service/            # Business logic
├── util/               # Utilities
├── *Controller.java    # JavaFX controllers
├── AuthType.java       # Authentication enum
└── Application classes
```

## Development Setup

### Prerequisites
- Java 11+
- Maven 3.6+
- JavaFX SDK

### Building
```bash
mvn clean compile      # Compile
mvn clean package      # Build JAR
mvn javafx:run        # Run in development
```

## Architecture Patterns

### MVC Pattern
- **Model**: Data classes in `model/` package
- **View**: FXML files in `resources/`
- **Controller**: Controller classes handling UI logic

### Service Layer
- `HttpClientService`: HTTP execution
- `ApiConfigurationService`: Config persistence
- `RequestHistoryService`: History management
- `BulkRequestService`: Bulk operations

### Async Programming
```java
CompletableFuture.supplyAsync(() -> httpClientService.executeRequest(request))
    .thenAccept(response -> Platform.runLater(() -> updateUI(response)));
```

## Key Components

### Loading Indicator Implementation
```java
private void showLoading(boolean loading) {
    if (loadingContainer != null) {
        loadingContainer.setVisible(loading);
        loadingContainer.setManaged(loading);
    }
    sendButton.setDisable(loading);
}
```

### Request Execution Flow
1. Build request from UI inputs
2. Validate request data
3. Show loading indicator
4. Execute request asynchronously
5. Update UI with response
6. Hide loading indicator
7. Save to history

### Configuration Persistence
- JSON serialization using Jackson
- User home directory storage
- Automatic backup and recovery

## Adding New Features

### New Authentication Type
1. Add enum value to `AuthType`
2. Update UI in `request-tab-content.fxml`
3. Add auth pane in controller
4. Implement auth logic in `HttpClientService`

### New Validation Rule
1. Add method to `ValidationUtils`
2. Create `ValidationResult` with error message
3. Integrate into request validation flow

### New Import Format
1. Create importer class following `CurlImporter` pattern
2. Add UI dialog for import
3. Integrate with request tab controller

## UI Development

### FXML Structure
- Main window: `rest-client-view.fxml`
- Request tabs: `request-tab-content.fxml`
- Dialogs: `bulk-request-view.fxml`, `curl-import-dialog.fxml`

### CSS Styling
- Bootstrap-inspired classes
- Status code color coding
- Loading spinner styling
- Responsive design principles

### Controller Communication
```java
// Parent-child controller communication
RequestTabController tabController = getControllerFromTab(selectedTab);
tabController.setParentController(this);
```

## Testing

### Unit Testing
- Test service classes independently
- Mock HTTP responses for testing
- Validate request building logic

### Integration Testing
- Test complete request flow
- Validate configuration persistence
- Test bulk request execution

### UI Testing
- TestFX for JavaFX UI testing
- Automated user interaction testing
- Response display validation

## Performance Optimization

### Memory Management
- Limit history size
- Clear large responses from memory
- Use weak references where appropriate

### Network Optimization
- Connection pooling
- Request timeout configuration
- Parallel execution for bulk requests

### UI Responsiveness
- Background thread execution
- Progress indicators
- Async UI updates

## Error Handling

### Exception Hierarchy
```java
ValidationException     # Data validation errors
ConfigurationException  # Config file errors
NetworkException       # HTTP request errors
```

### Error Reporting
- Logging with appropriate levels
- User-friendly error messages
- Error recovery mechanisms

## Code Style Guidelines

### Naming Conventions
- Classes: PascalCase
- Methods: camelCase
- Constants: UPPER_SNAKE_CASE
- Packages: lowercase

### JavaFX Best Practices
- Use FXML for UI definition
- Separate controller logic from UI
- Handle threading properly
- Use CSS for styling

### Documentation
- Javadoc for public APIs
- Inline comments for complex logic
- README updates for new features

## Build and Deployment

### Maven Configuration
- JavaFX plugin for development
- Assembly plugin for standalone JAR
- Dependency management

### Packaging
```bash
mvn clean package                    # Standard JAR
mvn clean package -P standalone      # Standalone JAR with dependencies
```

### Distribution
- Standalone JAR for easy distribution
- Platform-specific installers
- Docker containerization option

## Contributing

### Code Review Process
1. Create feature branch
2. Implement changes with tests
3. Update documentation
4. Submit pull request
5. Code review and approval

### Commit Guidelines
- Clear, descriptive commit messages
- Atomic commits for single features
- Reference issue numbers

### Issue Tracking
- Bug reports with reproduction steps
- Feature requests with use cases
- Documentation improvements

## Debugging

### Common Issues
- JavaFX threading violations
- FXML loading errors
- Configuration file corruption
- Network connectivity problems

### Debugging Tools
- IDE debugger for step-through debugging
- JavaFX Scene Builder for UI design
- Network monitoring tools
- Log file analysis

### Performance Profiling
- JProfiler for memory analysis
- Network request timing
- UI responsiveness measurement