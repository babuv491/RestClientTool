# Changelog

## [Latest] - Current Development

### Added
- Loading indicator with spinner and text during API calls
- Visual feedback shows "Sending request..." with green-themed spinner
- Improved user experience with disabled button during execution

### Changed
- Loading indicator positioned next to Send button for better visibility
- Simplified loading state management
- Enhanced CSS styling for loading components

### Technical Details
- Added `loadingContainer` HBox with ProgressIndicator and Label
- Implemented `showLoading(boolean)` method in RequestTabController
- Updated FXML structure for better loading indicator placement
- Green color theme (#28a745) for loading components

## [1.0.0] - Initial Release

### Core Features
- HTTP Methods Support: GET, POST, PUT, DELETE, PATCH
- Request Configuration: Headers, query parameters, request body
- Authentication: Basic, Bearer Token, API Key, OAuth 2.0
- Response Viewing: Pretty-printed JSON, raw response, headers
- Request History: Automatic tracking with timestamps

### Advanced Features
- Configuration Management: Save, load, delete API configurations
- Bulk Request Execution: Sequential and parallel execution
- Request Tabs: Multiple request tabs for workflow efficiency
- cURL Import: Import requests from cURL commands
- Detailed Logging: Request and response logging

### User Interface
- Modern Bootstrap-styled interface
- Dark theme support
- Responsive design with resizable panels
- Color-coded HTTP status codes
- Progress tracking for bulk requests

### Technical Implementation
- JavaFX-based desktop application
- MVC architecture with service layer
- Asynchronous request execution
- JSON configuration persistence
- Cross-platform compatibility

### Dependencies
- JavaFX for UI framework
- Apache HttpClient for HTTP communication
- Jackson for JSON processing
- BootstrapFX for UI styling

### Storage
- Configuration storage in user home directory
- Windows: `%USERPROFILE%\.restclienttool\`
- Linux/Mac: `~/.restclienttool/`

### Build System
- Maven-based build system
- Standalone JAR generation
- Development and production profiles

 