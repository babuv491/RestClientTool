# Deployment Guide

## Building the Executable JAR

### Prerequisites
- Java 11 or higher installed
- JAVA_HOME environment variable set

### Build Steps

1. **Using the build script (Recommended):**
   ```cmd
   build.bat
   ```

2. **Manual build:**
   ```cmd
   set JAVA_HOME=C:\Program Files\Java\jdk-11
   mvnw.cmd clean package
   ```

### Output
The executable JAR will be created at: `target\RestClientTool-standalone.jar`

## Running the Application

### Option 1: Direct execution (Recommended)
```cmd
java -jar target\RestClientTool-standalone.jar
```

### Option 2: With JavaFX modules (if Option 1 fails)
```cmd
java --add-modules javafx.controls,javafx.fxml -jar target\RestClientTool-standalone.jar
```

### Option 3: Using launcher script
```cmd
RestClientTool.bat
```

## Distribution

The `RestClientTool-standalone.jar` file contains all dependencies and can be distributed as a single file. Recipients will need:

1. Java 11+ installed
2. JavaFX runtime (if not included with Java distribution)
3. Command to run:
   ```cmd
   java --module-path "path\to\javafx\lib" --add-modules javafx.controls,javafx.fxml -jar RestClientTool-standalone.jar
   ```

## Troubleshooting

### Common Issues:
1. **JAVA_HOME not set**: Set environment variable or use build.bat
2. **JavaFX not found**: Download JavaFX SDK and specify --module-path
3. **Build fails**: Check Java version (requires 11+)

### File Locations:
- Executable JAR: `target\RestClientTool-standalone.jar`
- Configuration files: `%USERPROFILE%\.restclienttool\`
- Build logs: Console output during build process