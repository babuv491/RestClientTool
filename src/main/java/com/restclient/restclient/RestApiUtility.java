package com.restclient.restclient;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.util.Map;

public class RestApiUtility {

    // Base URI configuration
    private String baseUri;
    private RequestSpecification request;

    // Constructor to set the base URI
    public RestApiUtility(String baseUri) {
        this.baseUri = baseUri;
        RestAssured.baseURI = baseUri;
        this.request = RestAssured.given();
    }

    // Method to add headers
    public RestApiUtility setHeaders(Map<String, String> headers) {
        request.headers(headers);
        return this;
    }

    // Method to add query parameters
    public RestApiUtility setQueryParams(Map<String, String> queryParams) {
        request.queryParams(queryParams);
        return this;
    }

    // Method to set request body (used for POST, PUT, PATCH)
    public RestApiUtility setBody(String body) {
        request.body(body);
        return this;
    }

    // Method for Basic Authentication
    public RestApiUtility setBasicAuth(String username, String password) {
        request.auth().basic(username, password);
        return this;
    }

    // Method for Bearer Token Authentication
    public RestApiUtility setBearerToken(String token) {
        request.header("Authorization", "Bearer " + token);
        return this;
    }

    // Method to send GET request
    public Response sendGet(String endpoint) {
        return request.get(endpoint);
    }

    // Method to send POST request
    public Response sendPost(String endpoint) {
        return request.post(endpoint);
    }

    // Method to send PUT request
    public Response sendPut(String endpoint) {
        return request.put(endpoint);
    }

    // Method to send DELETE request
    public Response sendDelete(String endpoint) {
        return request.delete(endpoint);
    }

    // Method to send PATCH request
    public Response sendPatch(String endpoint) {
        return request.patch(endpoint);
    }

    // Method to send HEAD request
    public Response sendHead(String endpoint) {
        return request.head(endpoint);
    }

    // Method to send OPTIONS request
    public Response sendOptions(String endpoint) {
        return request.options(endpoint);
    }
}