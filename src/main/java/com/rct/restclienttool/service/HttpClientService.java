package com.rct.restclienttool.service;

import com.rct.restclienttool.AuthType;
import com.rct.restclienttool.model.HttpRequest;
import com.rct.restclienttool.model.HttpResponse;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
* Service class for executing HTTP requests with authentication support.
* Handles various authentication types and HTTP methods.
*
 * @author REST Client Tool
* @version 1.0
* @since 1.0
*/
public class HttpClientService {
    private static final Logger LOGGER = Logger.getLogger(HttpClientService.class.getName());

    /**
     * Executes an HTTP request with authentication and returns the response.
     *
     * @param request the HTTP request to execute
     * @return the HTTP response containing status, headers, and body
     */
    public HttpResponse executeRequest(HttpRequest request) {
        long startTime = System.currentTimeMillis();
       
        try {
            RequestSpecification requestSpec = RestAssured.given();
           
            // Set headers
            if (request.getHeaders() != null) {
                request.getHeaders().forEach(requestSpec::header);
            }
           
            // Set authentication
            configureAuthentication(requestSpec, request);
           
            // Set body if present
            if (request.getBody() != null && !request.getBody().isEmpty()) {
                requestSpec.body(request.getBody());
            }
           
            // Set query parameters
            if (request.getQueryParams() != null) {
                request.getQueryParams().forEach(requestSpec::queryParam);
            }
           
            // Execute request
            Response restResponse = executeRestRequest(requestSpec, request);
           
            // Create response object
            HttpResponse response = new HttpResponse();
            response.setStatusCode(restResponse.getStatusCode());
            response.setBody(restResponse.getBody().asString());
           
            // Extract headers
            Map<String, String> headers = new HashMap<>();
            restResponse.getHeaders().forEach(header ->
                headers.put(header.getName(), header.getValue())
            );
            response.setHeaders(headers);
           
            response.setResponseTimeMs(System.currentTimeMillis() - startTime);
            return response;
           
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error executing request", e);
            HttpResponse errorResponse = new HttpResponse();
            errorResponse.setStatusCode(-1);
            errorResponse.setBody("Error: " + e.getMessage());
            errorResponse.setResponseTimeMs(System.currentTimeMillis() - startTime);
            return errorResponse;
        }
    }

    /**
     * Configures authentication for the request based on the auth type.
     *
     * @param requestSpec the REST Assured request specification
     * @param request the HTTP request containing auth parameters
     */
    private void configureAuthentication(RequestSpecification requestSpec, HttpRequest request) {
        if (request.getAuthType() == null || request.getAuthParams() == null) {
            return;
        }
       
        switch (request.getAuthType()) {
            case BASIC:
                String username = request.getAuthParams().get("username");
                String password = request.getAuthParams().get("password");
                if (username != null && password != null) {
                    requestSpec.auth().basic(username, password);
                }
                break;
               
            case BEARER:
                String token = request.getAuthParams().get("token");
                if (token != null && !token.isEmpty()) {
                    requestSpec.header("Authorization", "Bearer " + token);
                }
                break;
               
            case API_KEY:
                String keyName = request.getAuthParams().get("keyName");
                String keyValue = request.getAuthParams().get("keyValue");
                String keyLocation = request.getAuthParams().get("keyLocation");
               
                if (keyName != null && keyValue != null) {
                    if ("header".equals(keyLocation)) {
                        requestSpec.header(keyName, keyValue);
                    } else if ("query".equals(keyLocation)) {
                        requestSpec.queryParam(keyName, keyValue);
                    }
                }
                break;
               
            case OAUTH2:
                String accessToken = request.getAuthParams().get("accessToken");
                if (accessToken != null && !accessToken.isEmpty()) {
                    requestSpec.header("Authorization", "Bearer " + accessToken);
                }
                break;
        }
    }

    /**
     * Executes the REST request using the appropriate HTTP method.
     *
     * @param requestSpec the configured request specification
     * @param request the HTTP request containing method and URL
     * @return the REST Assured response
     * @throws IllegalArgumentException if the HTTP method is not supported
     */
    private Response executeRestRequest(RequestSpecification requestSpec, HttpRequest request) {
        String url = request.getUrl();
       
        switch (request.getMethod().toUpperCase()) {
            case "GET":
                return requestSpec.get(url);
            case "POST":
                return requestSpec.post(url);
            case "PUT":
                return requestSpec.put(url);
            case "DELETE":
                return requestSpec.delete(url);
            case "PATCH":
                return requestSpec.patch(url);
            case "HEAD":
                return requestSpec.head(url);
            case "OPTIONS":
                return requestSpec.options(url);
            default:
                throw new IllegalArgumentException("Unsupported HTTP method: " + request.getMethod());
        }
    }
}