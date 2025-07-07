package com.rct.restclienttool.model;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
* Represents a historical request entry containing request details,
* response information, and timestamp for tracking API call history.
*
 * @author REST Client Tool
* @version 1.0
* @since 1.0
*/
public class RequestHistoryEntry implements Serializable {
    private static final long serialVersionUID = 1L;
   
    private String url;
    private String method;
    private int statusCode;
    private LocalDateTime timestamp;
    private long responseTimeMs;
    private HttpRequest request;
    private HttpResponse response;

    /**
     * Default constructor that sets the timestamp to the current time.
     */
    public RequestHistoryEntry() {
        this.timestamp = LocalDateTime.now();
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public long getResponseTimeMs() {
        return responseTimeMs;
    }

    public void setResponseTimeMs(long responseTimeMs) {
        this.responseTimeMs = responseTimeMs;
    }

    public HttpRequest getRequest() {
        return request;
    }

    public void setRequest(HttpRequest request) {
        this.request = request;
    }

    public HttpResponse getResponse() {
        return response;
    }

    public void setResponse(HttpResponse response) {
        this.response = response;
    }

    /**
     * Returns a formatted string representation of the history entry.
     *
     * @return formatted string with timestamp, method, URL, and status code
     */
    @Override
    public String toString() {
        return String.format("[%s] %s %s - %d",
            timestamp.toString(), method, url, statusCode);
    }
}
