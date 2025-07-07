package com.rct.restclienttool.service;

import com.rct.restclienttool.model.BulkRequest;
import com.rct.restclienttool.model.BulkResponse;
import com.rct.restclienttool.model.HttpRequest;
import com.rct.restclienttool.model.HttpResponse;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
* Service class for executing bulk HTTP requests either sequentially or in parallel.
* Provides functionality to execute multiple requests and generate summary statistics.
*
 * @author REST Client Tool
* @version 1.0
* @since 1.0
*/
public class BulkRequestService {

    /**
     * Executes a bulk request containing multiple HTTP requests.
     *
     * @param bulkRequest the bulk request configuration
     * @return bulk response containing all individual responses and summary
     */
    public BulkResponse executeBulkRequest(BulkRequest bulkRequest) {
        long startTime = System.currentTimeMillis();
        List<HttpResponse> responses;
       
        if (bulkRequest.isRunSequentially()) {
            responses = executeSequentially(bulkRequest);
        } else {
            responses = executeParallel(bulkRequest);
        }
       
        long totalTime = System.currentTimeMillis() - startTime;
        BulkResponse bulkResponse = new BulkResponse(responses, totalTime);
        bulkResponse.setSummary(generateSummary(responses));
       
        return bulkResponse;
    }
   
    /**
     * Executes requests sequentially with optional delays between requests.
     *
     * @param bulkRequest the bulk request configuration
     * @return list of HTTP responses in execution order
     */
    private List<HttpResponse> executeSequentially(BulkRequest bulkRequest) {
        List<HttpResponse> responses = new ArrayList<>();
       
        for (HttpRequest request : bulkRequest.getRequests()) {
            HttpResponse response = executeRequest(request);
            responses.add(response);
           
            if (bulkRequest.getDelayBetweenRequests() > 0) {
                try {
                    Thread.sleep(bulkRequest.getDelayBetweenRequests());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
       
        return responses;
    }
   
    /**
     * Executes requests in parallel using a thread pool.
     *
     * @param bulkRequest the bulk request configuration
     * @return list of HTTP responses (order may vary)
     */
    private List<HttpResponse> executeParallel(BulkRequest bulkRequest) {
        ExecutorService executor = Executors.newFixedThreadPool(
                Math.min(bulkRequest.getRequests().size(), 10));
       
        List<CompletableFuture<HttpResponse>> futures = bulkRequest.getRequests().stream()
                .map(request -> CompletableFuture.supplyAsync(() -> executeRequest(request), executor))
                .collect(Collectors.toList());
       
        List<HttpResponse> responses = futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());
       
        executor.shutdown();
        return responses;
    }
   
    /**
     * Executes a single HTTP request.
     *
     * @param request the HTTP request to execute
     * @return the HTTP response
     */
    public HttpResponse executeRequest(HttpRequest request) {
        long startTime = System.currentTimeMillis();
       
        try {
            RequestSpecification requestSpec = RestAssured.given();
           
            // Set headers
            if (request.getHeaders() != null) {
                request.getHeaders().forEach(requestSpec::header);
            }
           
            // Set body if present
            if (request.getBody() != null && !request.getBody().isEmpty()) {
                requestSpec.body(request.getBody());
            }
           
            // Set query parameters
            if (request.getQueryParams() != null) {
                request.getQueryParams().forEach(requestSpec::queryParam);
            }
           
            // Execute request based on method
            Response restResponse;
            String url = request.getUrl();
           
            switch (request.getMethod().toUpperCase()) {
                case "GET":
                    restResponse = requestSpec.get(url);
                    break;
                case "POST":
                    restResponse = requestSpec.post(url);
                    break;
                case "PUT":
                    restResponse = requestSpec.put(url);
                    break;
                case "DELETE":
                    restResponse = requestSpec.delete(url);
                    break;
                case "PATCH":
                    restResponse = requestSpec.patch(url);
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported HTTP method: " + request.getMethod());
            }
           
            // Create response object
            HttpResponse response = new HttpResponse();
            response.setStatusCode(restResponse.getStatusCode());
            response.setBody(restResponse.getBody().asString());
           
            // Extract headers
            Map<String, String> headers = new HashMap<>();
            restResponse.getHeaders().forEach(header -> headers.put(header.getName(), header.getValue()));
            response.setHeaders(headers);
           
            // Set execution time
            long executionTime = System.currentTimeMillis() - startTime;
            response.setExecutionTime(executionTime);
           
            return response;
        } catch (Exception e) {
            HttpResponse errorResponse = new HttpResponse();
            errorResponse.setStatusCode(-1);
            errorResponse.setBody("Error: " + e.getMessage());
            errorResponse.setExecutionTime(System.currentTimeMillis() - startTime);
            return errorResponse;
        }
    }
   
    /**
     * Generates summary statistics from a list of HTTP responses.
     *
     * @param responses the list of HTTP responses
     * @return map containing summary statistics
     */
    private Map<String, Object> generateSummary(List<HttpResponse> responses) {
        Map<String, Object> summary = new HashMap<>();
       
        // Count responses by status code
        Map<Integer, Long> statusCodeCounts = responses.stream()
                .collect(Collectors.groupingBy(HttpResponse::getStatusCode, Collectors.counting()));
        summary.put("statusCodeCounts", statusCodeCounts);
       
        // Calculate average response time
        double avgResponseTime = responses.stream()
               .mapToLong(HttpResponse::getExecutionTime)
                .average()
                .orElse(0);
        summary.put("averageResponseTime", avgResponseTime);
       
        // Find min and max response times
        long minResponseTime = responses.stream()
                .mapToLong(HttpResponse::getExecutionTime)
                .min()
                .orElse(0);
        summary.put("minResponseTime", minResponseTime);
       
        long maxResponseTime = responses.stream()
                .mapToLong(HttpResponse::getExecutionTime)
                .max()
                .orElse(0);
        summary.put("maxResponseTime", maxResponseTime);
       
        // Count successful and failed requests
        long successfulRequests = responses.stream()
                .filter(r -> r.getStatusCode() >= 200 && r.getStatusCode() < 300)
                .count();
        summary.put("successfulRequests", successfulRequests);
       
        long failedRequests = responses.size() - successfulRequests;
        summary.put("failedRequests", failedRequests);
       
        return summary;
    }
}