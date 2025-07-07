package com.rct.restclienttool.util;

import com.rct.restclienttool.AuthType;
import com.rct.restclienttool.model.HttpRequest;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
* Utility class for validating inputs in the REST client tool
*/
public class ValidationUtils {
   
    private static final Pattern URL_PATTERN = Pattern.compile("^(https?|ftp)://[^\\s/$.?#].[^\\s]*$");
    private static final Pattern JSON_PATTERN = Pattern.compile("^\\s*[{\\[].*[}\\]]\\s*$", Pattern.DOTALL);
    private static final Pattern XML_PATTERN = Pattern.compile("^\\s*<[^>]+>.*</[^>]+>\\s*$", Pattern.DOTALL);
   
    /**
     * Validates a URL string for proper format and structure.
     *
     * @param url the URL to validate
     * @return validation result with success status and message
     */
    public static ValidationResult validateUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            return new ValidationResult(false, "URL cannot be empty");
        }
       
        if (!URL_PATTERN.matcher(url).matches()) {
            return new ValidationResult(false, "Invalid URL format. URL must start with http:// or https://");
        }
       
        try {
            new URL(url);
            return new ValidationResult(true, "Valid URL");
        } catch (MalformedURLException e) {
            return new ValidationResult(false, "Malformed URL: " + e.getMessage());
        }
    }
   
    /**
     * Validate request headers
     * @param headers Map of header name/value pairs
     * @return A validation result with success/error message
     */
    public static ValidationResult validateHeaders(Map<String, String> headers) {
        if (headers == null) {
            return new ValidationResult(false, "Headers cannot be null");
        }
       
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            String name = entry.getKey();
            if (name == null || name.trim().isEmpty()) {
                return new ValidationResult(false, "Header name cannot be empty");
            }
           
            // Check for invalid characters in header name
            if (name.contains(":") || name.contains("\n") || name.contains("\r")) {
                return new ValidationResult(false, "Header name contains invalid characters: " + name);
            }
        }
        
        return new ValidationResult(true, "Valid headers");
    }
   
    /**
     * Validate request body based on content type
     * @param body The request body
     * @param contentType The content type
     * @return A validation result with success/error message
     */
    public static ValidationResult validateBody(String body, String contentType) {
        if (body == null || body.trim().isEmpty()) {
            return new ValidationResult(true, "Empty body");
        }
       
        if (contentType == null) {
            return new ValidationResult(true, "No content type specified");
        }
       
        if (contentType.contains("json")) {
            return validateJson(body);
        } else if (contentType.contains("xml")) {
            return validateXml(body);
        }
       
        return new ValidationResult(true, "Body validation not implemented for content type: " + contentType);
    }
   
    /**
     * Validate JSON string
     * @param json The JSON string to validate
     * @return A validation result with success/error message
     */
    public static ValidationResult validateJson(String json) {
        if (json == null || json.trim().isEmpty()) {
            return new ValidationResult(false, "JSON cannot be empty");
        }
       
        if (!JSON_PATTERN.matcher(json).matches()) {
            return new ValidationResult(false, "Invalid JSON format. Must start with { or [ and end with } or ]");
        }
       
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            mapper.readTree(json);
            return new ValidationResult(true, "Valid JSON");
        } catch (Exception e) {
            return new ValidationResult(false, "Invalid JSON: " + e.getMessage());
        }
    }
   
    /**
     * Validate XML string
     * @param xml The XML string to validate
     * @return A validation result with success/error message
     */
    public static ValidationResult validateXml(String xml) {
        if (xml == null || xml.trim().isEmpty()) {
            return new ValidationResult(false, "XML cannot be empty");
        }
       
        if (!XML_PATTERN.matcher(xml).matches()) {
            return new ValidationResult(false, "Invalid XML format. Must have matching opening and closing tags");
        }
       
        try {
            javax.xml.parsers.DocumentBuilderFactory factory = javax.xml.parsers.DocumentBuilderFactory.newInstance();
            javax.xml.parsers.DocumentBuilder builder = factory.newDocumentBuilder();
            org.xml.sax.InputSource is = new org.xml.sax.InputSource(new java.io.StringReader(xml));
            builder.parse(is);
            return new ValidationResult(true, "Valid XML");
        } catch (Exception e) {
            return new ValidationResult(false, "Invalid XML: " + e.getMessage());
        }
    }
   
    /**
     * Validate authentication parameters
     * @param authType The authentication type
     * @param authParams The authentication parameters
     * @return A validation result
     */
    public static ValidationResult validateAuthParams(AuthType authType, Map<String, String> authParams) {
        if (authParams == null) {
            return new ValidationResult(false, "Authentication parameters cannot be null");
        }
       
        switch (authType) {
            case BASIC:
                String username = authParams.get("username");
                if (username == null || username.trim().isEmpty()) {
                    return new ValidationResult(false, "Username cannot be empty for Basic authentication");
                }
                break;
               
            case BEARER:
                String token = authParams.get("token");
                if (token == null || token.trim().isEmpty()) {
                    return new ValidationResult(false, "Token cannot be empty for Bearer authentication");
                }
                break;
               
            case API_KEY:
                String keyName = authParams.get("keyName");
                String keyValue = authParams.get("keyValue");
                String keyLocation = authParams.get("keyLocation");
               
                if (keyName == null || keyName.trim().isEmpty()) {
                    return new ValidationResult(false, "Key name cannot be empty for API Key authentication");
                }
               
                if (keyValue == null || keyValue.trim().isEmpty()) {
                    return new ValidationResult(false, "Key value cannot be empty for API Key authentication");
                }
               
                if (keyLocation == null || keyLocation.trim().isEmpty()) {
                    return new ValidationResult(false, "Key location cannot be empty for API Key authentication");
                }
               
                if (!keyLocation.equals("header") && !keyLocation.equals("query")) {
                    return new ValidationResult(false, "Key location must be 'header' or 'query' for API Key authentication");
                }
                break;
               
            case OAUTH2:
                String accessToken = authParams.get("accessToken");
                if (accessToken == null || accessToken.trim().isEmpty()) {
                    return new ValidationResult(false, "Access token cannot be empty for OAuth 2.0 authentication");
                }
                break;
        }
       
        return new ValidationResult(true, "Valid authentication parameters");
    }
   
    /**
     * Validate a complete HTTP request
     * @param request The HTTP request to validate
     * @return A list of validation results
     */
    public static List<ValidationResult> validateRequest(HttpRequest request) {
        List<ValidationResult> results = new ArrayList<>();
       
        // Validate URL
        results.add(validateUrl(request.getUrl()));
       
        // Validate headers
        results.add(validateHeaders(request.getHeaders()));
       
        // Validate body if present
        String contentType = request.getHeaders().get("Content-Type");
        if (contentType == null) {
            contentType = request.getHeaders().get("content-type");
        }
       
        if (request.getBody() != null && !request.getBody().isEmpty()) {
            results.add(validateBody(request.getBody(), contentType));
        }
       
        // Validate authentication parameters
        if (request.getAuthType() != AuthType.NONE) {
            results.add(validateAuthParams(request.getAuthType(), request.getAuthParams()));
        }
       
        return results;
    }
   
    /**
     * Class to represent a validation result
     */
    public static class ValidationResult {
        private final boolean valid;
        private final String message;
       
        public ValidationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }
       
        public boolean isValid() {
            return valid;
        }
       
        public String getMessage() {
            return message;
        }
       
        @Override
        public String toString() {
            return message;
        }
    }
}