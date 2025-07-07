# REST Client Tool - User Guide

## Getting Started

### Installation
1. Ensure Java 11+ is installed
2. Download the application JAR file
3. Run: `java -jar RestClientTool-standalone.jar`

### First Request
1. Enter URL in the URL field
2. Select HTTP method (GET, POST, etc.)
3. Click "Send Request"
4. View response in the right panel

## Interface Overview

### Main Window
- **Left Panel**: Request configuration (URL, method, headers, body, auth)
- **Right Panel**: Response display (body, headers, status)
- **Top Toolbar**: Save/Load configs, History, Bulk requests

### Request Tabs
- Multiple request tabs for different APIs
- Click "+" to add new tab
- Each tab maintains independent configuration

## Making Requests

### Basic Request
1. **URL**: Enter the API endpoint
2. **Method**: Choose HTTP method from dropdown
3. **Send**: Click "Send Request" button
4. **Loading**: Spinner shows "Sending request..." during execution

### Adding Headers
1. Go to "Headers" tab
2. Click "Add Header"
3. Enter header name and value
4. Check/uncheck to enable/disable headers

### Query Parameters
1. Go to "Params" tab
2. Click "Add Parameter"
3. Enter parameter name and value
4. Parameters automatically sync with URL

### Request Body
1. Go to "Body" tab
2. Select content type (JSON, XML, etc.)
3. Enter request body content
4. Use "Format JSON" to pretty-print JSON

### Authentication
1. Go to "Auth" tab
2. Select authentication type:
    - **None**: No authentication
    - **Basic Auth**: Username/password
    - **Bearer Token**: Authorization token
    - **API Key**: Key name/value in header or query
    - **OAuth 2.0**: Access token

## Response Viewing

### Response Body
- **Pretty Print**: Format JSON responses
- **Raw**: View unformatted response
- Automatic JSON formatting for valid JSON

### Response Headers
- View all response headers in table format
- Headers tab shows name/value pairs

### Status Information
- Status code with color coding:
    - Green: 2xx success
    - Yellow: 3xx redirects
    - Red: 4xx/5xx errors
- Response time in milliseconds

## Configuration Management

### Saving Configurations
1. Configure your request (URL, method, headers, etc.)
2. Click "Save Config" in toolbar
3. Enter a name for the configuration
4. Configuration saved to disk

### Loading Configurations
1. Click "Load Config" in toolbar
2. Select from list of saved configurations
3. Configuration loads into current tab

### Deleting Configurations
1. Click "Delete Config" in toolbar
2. Select configuration to delete
3. Confirm deletion

## Request History

### Viewing History
1. Click "History" in toolbar
2. View all previously executed requests
3. Shows method, URL, status code, timestamp

### Loading from History
1. Select a request from history
2. Click "Load Selected Request"
3. Creates new tab with historical request
4. Response also loaded for viewing

### Clearing History
1. In history dialog, click "Clear History"
2. Removes all historical entries

## Bulk Requests

### Creating Bulk Requests
1. Click "Bulk Requests" in toolbar
2. Add multiple requests to the list
3. Configure each request individually

### Execution Options
- **Sequential**: Execute one after another with delay
- **Parallel**: Execute all simultaneously
- **Delay**: Set delay between sequential requests

### Viewing Results
- Summary shows success/failure counts
- Individual response inspection
- Total execution time

## Advanced Features

### cURL Import
1. Right-click in URL field
2. Select "Import from cURL"
3. Paste cURL command
4. Request automatically configured

### Request Logs
1. Click "View Logs" in toolbar
2. See detailed request/response logs
3. Includes headers, body, timing information

### Keyboard Shortcuts
- `Ctrl+N`: New request tab
- `Ctrl+S`: Save configuration
- `Ctrl+L`: Load configuration
- `F5`: Execute current request

## Tips and Best Practices

### URL Management
- URLs with query parameters automatically populate Params tab
- Editing params updates URL automatically
- Use proper URL encoding for special characters

### JSON Handling
- Use "Format JSON" for readable formatting
- JSON validation shows errors in real-time
- Content-Type header automatically added for JSON

### Error Handling
- Red status codes indicate errors
- Check response body for error details
- Validate request before sending

### Performance
- Use parallel execution for bulk performance testing
- Monitor response times for API performance
- History helps track API behavior over time

## Troubleshooting

### Common Issues
1. **Request fails**: Check URL format and network connectivity
2. **Authentication errors**: Verify credentials and auth type
3. **JSON errors**: Validate JSON syntax before sending
4. **Timeout**: Check network connection and API availability

### Configuration Issues
- Configurations stored in user home directory
- Check file permissions if save/load fails
- Clear history if application becomes slow

### UI Issues
- Restart application if tabs become unresponsive
- Check Java version compatibility
- Ensure sufficient memory for large responses