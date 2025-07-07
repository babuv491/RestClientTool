# REST Client Tool

A powerful and user-friendly REST API client built with JavaFX, designed for testing and debugging REST APIs with ease.

## Features

### Core Functionality
- **HTTP Methods Support**: GET, POST, PUT, DELETE, PATCH
- **Request Configuration**: Headers, query parameters, request body
- **Authentication**: Support for various authentication types
- **Response Viewing**: Pretty-printed JSON, raw response, headers inspection
- **Request History**: Automatic tracking of all requests with timestamps

### Advanced Features
- **Configuration Management**: Save, load, and delete API configurations
- **Bulk Request Execution**: Execute multiple requests sequentially or in parallel
- **Request Tabs**: Multiple request tabs for efficient workflow
- **Import/Export**: Import requests from cURL commands
- **Logging**: Detailed request and response logging

### User Interface
- **Modern UI**: Bootstrap-styled interface with dark theme support
- **Responsive Design**: Resizable panels and windows
- **Status Indicators**: Color-coded HTTP status codes
- **Progress Tracking**: Real-time execution progress for bulk requests

## Installation

### Prerequisites
- Java 11 or higher
- Maven 3.6 or higher

### Build from Source
```bash
git clone <repository-url>
cd RestClientTool
mvn clean package
```

### Run Standalone JAR
```bash
# Build the standalone JAR
mvn clean package

# Run the application (Windows)
run.bat

# Run the application (Linux/Mac)
./run.sh

# Or run directly with Java
java --module-path /path/to/javafx/lib --add-modules javafx.controls,javafx.fxml -jar target/RestClientTool-standalone.jar
```

### Run with Maven (Development)
```bash
mvn clean javafx:run
```

## Usage

### Basic Request
1. Enter the API endpoint URL
2. Select HTTP method (GET, POST, PUT, DELETE, PATCH)
3. Add headers if needed
4. Add request body for POST/PUT requests
5. Click "Send" to execute the request

### Configuration Management
- **Save Config**: Save current request configuration with a custom name
- **Load Config**: Load a previously saved configuration
- **Delete Config**: Remove saved configurations

### Bulk Requests
1. Click "Bulk Requests" in the toolbar
2. Add multiple requests to the list
3. Choose sequential or parallel execution
4. Set delay between requests (for sequential execution)
5. Execute and view detailed summary

### Request History
- View all previously executed requests
- Load historical requests into new tabs
- Clear history when needed

## Project Structure

```
src/
├── main/
│   ├── java/com/rct/restclienttool/
│   │   ├── model/                 # Data models
│   │   │   ├── ApiConfiguration.java
│   │   │   ├── BulkRequest.java
│   │   │   ├── BulkResponse.java
│   │   │   ├── HttpRequest.java
│   │   │   ├── HttpResponse.java
│   │   │   ├── RequestHistoryEntry.java
│   │   │   └── RequestTab.java
│   │   ├── service/               # Business logic
│   │   │   ├── ApiConfigurationService.java
│   │   │   ├── BulkRequestService.java
│   │   │   ├── HttpClientService.java
│   │   │   └── RequestHistoryService.java
│   │   ├── util/                  # Utilities
│   │   │   ├── CurlImporter.java
│   │   │   ├── JsonFormatter.java
│   │   │   └── ValidationUtils.java
│   │   ├── AppLauncher.java       # Application entry point
│   │   ├── RestClientApplication.java
│   │   ├── RestClientController.java
│   │   ├── BulkRequestController.java
│   │   └── RequestTabController.java
│   └── resources/com/rct/restclienttool/
│       ├── bulk-request-view.fxml
│       ├── rest-client-view.fxml
│       ├── request-tab-content.fxml
│       └── styles.css
```

## Dependencies

- **JavaFX**: UI framework
- **Rest Assured**: HTTP client for bulk requests
- **Jackson**: JSON processing
- **Apache HttpClient**: HTTP communication
- **BootstrapFX**: UI styling

## Configuration Storage

Configurations are stored in:
- Windows: `%USERPROFILE%\.restclienttool\`
- Linux/Mac: `~/.restclienttool/`

## Features in Detail

### Authentication Support
- No Authentication
- Basic Authentication
- Bearer Token
- API Key
- Custom headers

### Request History
- Automatic saving of all requests
- Timestamp tracking
- Status code recording
- Quick reload functionality

### Bulk Request Execution
- Sequential execution with configurable delays
- Parallel execution for performance testing
- Detailed execution summary with statistics
- Individual response inspection

### Import/Export
- Import from cURL commands
- Export configurations for sharing
- Batch import from files

## Keyboard Shortcuts

- `Ctrl+N`: New request tab
- `Ctrl+S`: Save configuration
- `Ctrl+L`: Load configuration
- `Ctrl+H`: Show history
- `F5`: Execute current request

## Troubleshooting

### Common Issues

1. **Application won't start**
   - Ensure Java 11+ is installed
   - Check JavaFX runtime is available

2. **Requests failing**
   - Verify URL format
   - Check network connectivity
   - Validate headers and authentication

3. **Configuration not saving**
   - Check write permissions in user directory
   - Ensure sufficient disk space

### Logging
Enable detailed logging by setting system property:
```bash
-Djava.util.logging.level=FINE
```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests if applicable
5. Submit a pull request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Support

For issues and feature requests, please create an issue in the project repository.

## Changelog

### Version 1.0.0
- Initial release
- Basic HTTP client functionality
- Configuration management
- Request history
- Bulk request execution
- Modern UI with Bootstrap styling

---

**Built with ❤️ using JavaFX and Rest Assured**