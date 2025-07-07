package com.rct.restclienttool.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
* Represents an HTTP response containing status code, headers, body,
* and execution time information.
*
 * @author REST Client Tool
* @version 1.0
* @since 1.0
*/
public class HttpResponse implements Serializable {
    private static final long serialVersionUID = 1L;
    private int statusCode;
    private Map<String, String> headers;
    private String body;
    private long responseTimeMs;

    /**
     * Default constructor that initializes the response with empty headers.
     */
    public HttpResponse() {
        this.headers = new HashMap<>();
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    /**
     * Adds a header to the response.
     *
     * @param key the header name
     * @param value the header value
     */
    public void addHeader(String key, String value) {
        this.headers.put(key, value);
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public long getResponseTimeMs() {
        return responseTimeMs;
    }

    public void setResponseTimeMs(long responseTimeMs) {
        this.responseTimeMs = responseTimeMs;
    }
   
    /**
     * Gets the execution time (alias for getResponseTimeMs).
     *
     * @return execution time in milliseconds
     */
    public long getExecutionTime() {
        return responseTimeMs;
    }
   
    /**
     * Sets the execution time (alias for setResponseTimeMs).
     *
     * @param executionTime execution time in milliseconds
     */
    public void setExecutionTime(long executionTime) {
        this.responseTimeMs = executionTime;
    }
}
