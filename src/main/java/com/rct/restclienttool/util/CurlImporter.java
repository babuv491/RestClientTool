package com.rct.restclienttool.util;

import com.rct.restclienttool.model.HttpRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
* Utility class for importing curl commands and converting them to HttpRequest objects
*/
public class CurlImporter {
   
    /**
     * Parse a curl command and convert it to an HttpRequest
     * @param curlCommand The curl command to parse
     * @return An HttpRequest object representing the curl command
     */
    public static HttpRequest parseCurlCommand(String curlCommand) {
        HttpRequest request = new HttpRequest();
       
        // Default to GET method
        request.setMethod("GET");
       
        // Clean up the command - handle line continuations and extra spaces
        curlCommand = curlCommand.replaceAll("\\\\\\s*\\n\\s*", " ").trim();
       
        // Remove any leading 'curl' command if present
        if (curlCommand.toLowerCase().startsWith("curl ")) {
            curlCommand = curlCommand.substring(5).trim();
        }
       
        // Extract URL - try different patterns
        String url = extractUrl("curl " + curlCommand); // Add curl prefix back for pattern matching
        if (url != null) {
            request.setUrl(url);
        } else {
            throw new IllegalArgumentException("Could not find URL in curl command");
        }
       
        // Extract method
        String method = extractMethod(curlCommand);
        if (method != null) {
            request.setMethod(method);
        }
       
        // Extract headers
        Map<String, String> headers = extractHeaders(curlCommand);
        request.setHeaders(headers);
       
        // Extract body
        String body = extractBody(curlCommand);
        if (body != null) {
            request.setBody(body);
           
            // If content-type is not specified but we have a body, try to detect or default to JSON
            if (!headers.containsKey("Content-Type") && !headers.containsKey("content-type")) {
                // Try to detect if it's JSON
                if (body.trim().startsWith("{") || body.trim().startsWith("[")) {
                    headers.put("Content-Type", "application/json");
                    request.setHeaders(headers);
                }
            }
           
            // If method is GET but we have a body, change to POST
            if (request.getMethod().equals("GET")) {
                request.setMethod("POST");
            }
        }
       
        return request;
    }
   
    private static String extractUrl(String curlCommand) {
        // Try different URL patterns
       
        // Pattern 1: URL at the end of the command (common in many examples)
        Pattern urlPattern1 = Pattern.compile("\\s+(https?://[^\\s\"']+)\\s*$");
        Matcher urlMatcher1 = urlPattern1.matcher(curlCommand);
        if (urlMatcher1.find()) {
            return urlMatcher1.group(1);
        }
       
        // Pattern 2: Standard URL after curl
        Pattern urlPattern2 = Pattern.compile("curl\\s+(?:-[A-Za-z]+\\s+[^-][^\\s]*\\s+)*[\"']?(https?://[^\\s\"']+)[\"']?");
        Matcher urlMatcher2 = urlPattern2.matcher(curlCommand);
        if (urlMatcher2.find()) {
            return urlMatcher2.group(1);
        }
       
        // Pattern 3: URL after --url or -L option
        Pattern urlPattern3 = Pattern.compile("(?:--url|-L)\\s+[\"']?(https?://[^\\s\"']+)[\"']?");
        Matcher urlMatcher3 = urlPattern3.matcher(curlCommand);
        if (urlMatcher3.find()) {
            return urlMatcher3.group(1);
        }
       
        // Pattern 4: Last resort - look for any URL
        Pattern urlPattern4 = Pattern.compile("(https?://[^\\s\"']+)");
        Matcher urlMatcher4 = urlPattern4.matcher(curlCommand);
        if (urlMatcher4.find()) {
            return urlMatcher4.group(1);
        }
       
        return null;
    }
   
    private static String extractMethod(String curlCommand) {
        // Look for -X or --request option
        Pattern methodPattern = Pattern.compile("(?:-X|--request)\\s+([A-Z]+)");
        Matcher methodMatcher = methodPattern.matcher(curlCommand);
        if (methodMatcher.find()) {
            return methodMatcher.group(1);
        }
       
        // Infer method from other options
        if (curlCommand.contains(" -d ") || curlCommand.contains(" --data ") ||
            curlCommand.contains(" --data-binary ") || curlCommand.contains(" --data-raw ")) {
            return "POST";
        }
       
        return null;
    }
   
    private static Map<String, String> extractHeaders(String curlCommand) {
        Map<String, String> headers = new HashMap<>();
       
        // Pattern for -H or --header with single quotes
        Pattern headerPattern1 = Pattern.compile("(?:-H|--header)\\s+'([^:]+):\\s*([^']*)'");
        Matcher headerMatcher1 = headerPattern1.matcher(curlCommand);
        while (headerMatcher1.find()) {
            headers.put(headerMatcher1.group(1), headerMatcher1.group(2));
        }
       
        // Pattern for -H or --header with double quotes
        Pattern headerPattern2 = Pattern.compile("(?:-H|--header)\\s+\"([^:]+):\\s*([^\"]*)\"");
        Matcher headerMatcher2 = headerPattern2.matcher(curlCommand);
        while (headerMatcher2.find()) {
            headers.put(headerMatcher2.group(1), headerMatcher2.group(2));
        }
       
        return headers;
    }
   
    private static String extractBody(String curlCommand) {
        // Try different body patterns
       
        // Pattern 1: --data, -d with single quotes
        Pattern bodyPattern1 = Pattern.compile("(?:--data|-d)\\s+'([^']*)'");
        Matcher bodyMatcher1 = bodyPattern1.matcher(curlCommand);
        if (bodyMatcher1.find()) {
            return bodyMatcher1.group(1);
        }
       
        // Pattern 2: --data, -d with double quotes
        Pattern bodyPattern2 = Pattern.compile("(?:--data|-d)\\s+\"([^\"]*)\"");
        Matcher bodyMatcher2 = bodyPattern2.matcher(curlCommand);
        if (bodyMatcher2.find()) {
            return bodyMatcher2.group(1);
        }
       
        // Pattern 3: --data-raw with quotes
        Pattern bodyPattern3 = Pattern.compile("--data-raw\\s+[\"']([^\"']*)[\"']");
        Matcher bodyMatcher3 = bodyPattern3.matcher(curlCommand);
        if (bodyMatcher3.find()) {
            return bodyMatcher3.group(1);
        }
       
        // Pattern 4: --data-binary with quotes
        Pattern bodyPattern4 = Pattern.compile("--data-binary\\s+[\"']([^\"']*)[\"']");
        Matcher bodyMatcher4 = bodyPattern4.matcher(curlCommand);
        if (bodyMatcher4.find()) {
            return bodyMatcher4.group(1);
        }
       
        // Pattern 5: --data, -d without quotes (more error-prone)
        Pattern bodyPattern5 = Pattern.compile("(?:--data|-d)\\s+(\\{[^}]*\\})");
        Matcher bodyMatcher5 = bodyPattern5.matcher(curlCommand);
        if (bodyMatcher5.find()) {
            return bodyMatcher5.group(1);
        }
       
        return null;
    }
   
    /**
     * Validate if a string is a valid curl command
     * @param command The command to validate
     * @return true if the command is a valid curl command
     */
    public static boolean isValidCurlCommand(String command) {
        return command != null && command.trim().toLowerCase().startsWith("curl ");
    }
}