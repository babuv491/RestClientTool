@echo off
echo Setting up Java environment...
set JAVA_HOME=C:\Program Files\Java\jdk-11
set PATH=%JAVA_HOME%\bin;%PATH%

echo Building REST Client Tool JAR...
cmd /c mvnw.cmd clean package -DskipTests

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo BUILD SUCCESSFUL!
    echo ========================================
    echo Executable JAR created at: target\RestClientTool-standalone.jar
    echo.
    echo To run the application:
    echo java --module-path "path\to\javafx\lib" --add-modules javafx.controls,javafx.fxml -jar target\RestClientTool-standalone.jar
    echo.
) else (
    echo.
    echo ========================================
    echo BUILD FAILED!
    echo ========================================
    echo Please check the error messages above.
)