@echo off
echo Starting REST Client Tool...
java -jar RestClientTool-standalone.jar
if %ERRORLEVEL% NEQ 0 (
    echo Failed to start application. Error code: %ERRORLEVEL%
    pause
)