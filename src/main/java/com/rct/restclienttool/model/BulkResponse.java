package com.rct.restclienttool.model;

import java.util.List;
import java.util.Map;

/**
* Represents the response from a bulk request execution containing
* all individual responses, execution time, and summary statistics.
*
 * @author REST Client Tool
* @version 1.0
* @since 1.0
*/
public class BulkResponse {
    private List<HttpResponse> responses;
    private long totalExecutionTime;
    private Map<String, Object> summary;

    /**
     * Constructs a new BulkResponse with responses and execution time.
     *
     * @param responses the list of HTTP responses from executed requests
     * @param totalExecutionTime total execution time in milliseconds
     */
    public BulkResponse(List<HttpResponse> responses, long totalExecutionTime) {
        this.responses = responses;
        this.totalExecutionTime = totalExecutionTime;
    }

    /**
     * Gets the list of HTTP responses from executed requests.
     *
     * @return the list of HTTP responses
     */
    public List<HttpResponse> getResponses() {
        return responses;
    }

    /**
     * Sets the list of HTTP responses.
     *
     * @param responses the list of HTTP responses
     */
    public void setResponses(List<HttpResponse> responses) {
        this.responses = responses;
    }

    /**
     * Gets the total execution time for all requests.
     *
     * @return total execution time in milliseconds
     */
    public long getTotalExecutionTime() {
        return totalExecutionTime;
    }

    /**
     * Sets the total execution time for all requests.
     *
     * @param totalExecutionTime total execution time in milliseconds
     */
    public void setTotalExecutionTime(long totalExecutionTime) {
        this.totalExecutionTime = totalExecutionTime;
    }

    /**
     * Gets the summary statistics for the bulk request execution.
     *
     * @return map containing summary statistics
     */
    public Map<String, Object> getSummary() {
        return summary;
    }

    /**
     * Sets the summary statistics for the bulk request execution.
     *
     * @param summary map containing summary statistics
     */
    public void setSummary(Map<String, Object> summary) {
        this.summary = summary;
    }
}
