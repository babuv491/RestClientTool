@echo off
echo Starting REST Client Tool...

REM Try with explicit JavaFX module path first
java --module-path . --add-modules javafx.controls,javafx.fxml,javafx.base,javafx.graphics -jar RestClientTool-standalone.jar

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo Trying alternative launch method...
    java -Djava.awt.headless=false --add-opens javafx.graphics/javafx.scene=ALL-UNNAMED -jar RestClientTool-standalone.jar
)

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo Trying basic launch...
    java -jar RestClientTool-standalone.jar
)

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo All launch methods failed. Please ensure Java 11+ is installed.
)
pause