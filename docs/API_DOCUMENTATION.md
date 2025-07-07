# REST Client Tool - API Documentation

## Model Classes

### HttpRequest
Represents an HTTP request with URL, method, headers, body, and authentication.

**Fields:**
- `String url` - Target URL
- `String method` - HTTP method (GET, POST, PUT, DELETE, PATCH)
- `Map<String, String> headers` - HTTP headers
- `String body` - Request body
- `AuthType authType` - Authentication type
- `Map<String, String> authParams` - Auth parameters

**Key Methods:**
- `addHeader(String key, String value)` - Add HTTP header
- `addAuthParam(String key, String value)` - Add auth parameter

### HttpResponse
Represents HTTP response with status, headers, body, and timing.

**Fields:**
- `int statusCode` - HTTP status code
- `Map<String, String> headers` - Response headers
- `String body` - Response body
- `long responseTimeMs` - Response time in milliseconds

### AuthType (Enum)
Authentication types: NONE, BASIC, BEARER, API_KEY, OAUTH2

## Service Classes

### HttpClientService
Executes HTTP requests and returns responses.

**Methods:**
- `HttpResponse executeRequest(HttpRequest request)` - Execute HTTP request

### ApiConfigurationService
Manages saving/loading API configurations.

**Methods:**
- `saveConfiguration(ApiConfiguration config)` - Save config
- `loadAllConfigurations()` - Load all configs
- `deleteConfiguration(String name)` - Delete config

### RequestHistoryService
Manages request history persistence.

**Methods:**
- `addHistoryEntry(RequestHistoryEntry entry)` - Add to history
- `loadHistory()` - Load all history
- `clearHistory()` - Clear history

## Controller Classes

### RestClientController
Main application controller managing UI and tabs.

**Key Methods:**
- `updateResponseView(HttpResponse response)` - Update response display
- `handleSaveConfig()` - Save configuration
- `handleLoadConfig()` - Load configuration
- `handleShowHistory()` - Show request history

### RequestTabController
Controls individual request tabs with form inputs and execution.

**Key Methods:**
- `handleSendRequest()` - Execute HTTP request with loading indicator
- `buildRequest()` - Build HttpRequest from UI inputs
- `showLoading(boolean loading)` - Show/hide loading spinner
- `loadConfiguration(ApiConfiguration config)` - Load config into UI

## Utility Classes

### JsonFormatter
JSON formatting and validation utilities.

**Methods:**
- `prettyPrint(String json)` - Format JSON with indentation
- `isValidJson(String json)` - Validate JSON syntax

### ValidationUtils
Request and data validation.

**Methods:**
- `validateUrl(String url)` - Validate URL format
- `validateJson(String json)` - Validate JSON content
- `validateRequest(HttpRequest request)` - Validate complete request

### CurlImporter
Imports cURL commands to HttpRequest objects.

**Methods:**
- `importFromCurl(String curlCommand)` - Parse cURL command

## Application Classes

### RestClientApplication
Main JavaFX application class that initializes the UI.

### Launcher
Application entry point with system property configuration.

## Key Features

### Loading Indicator
- Spinner appears next to Send button during API calls
- Shows "Sending request..." text
- Button disabled during execution
- Green theme matching Send button

### Configuration Storage
- Windows: `%USERPROFILE%\.restclienttool\`
- Linux/Mac: `~/.restclienttool/`

### Threading
- HTTP requests execute on background threads
- UI updates on JavaFX Application Thread
- CompletableFuture for async operations