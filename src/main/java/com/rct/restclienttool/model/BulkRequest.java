package com.rct.restclienttool.model;

import java.util.List;

/**
* Represents a bulk request configuration containing multiple HTTP requests
* and execution parameters such as sequential vs parallel execution and delays.
*
 * @author REST Client Tool
* @version 1.0
* @since 1.0
*/
public class BulkRequest {
    private List<HttpRequest> requests;
    private boolean runSequentially;
    private int delayBetweenRequests; // in milliseconds

    /**
     * Constructs a new BulkRequest with the specified parameters.
     *
     * @param requests the list of HTTP requests to execute
     * @param runSequentially true to run requests sequentially, false for parallel
     * @param delayBetweenRequests delay in milliseconds between sequential requests
     */
    public BulkRequest(List<HttpRequest> requests, boolean runSequentially, int delayBetweenRequests) {
        this.requests = requests;
        this.runSequentially = runSequentially;
        this.delayBetweenRequests = delayBetweenRequests;
    }

    /**
     * Gets the list of HTTP requests to execute.
     *
     * @return the list of HTTP requests
     */
    public List<HttpRequest> getRequests() {
        return requests;
    }

    /**
     * Sets the list of HTTP requests to execute.
     *
     * @param requests the list of HTTP requests
     */
    public void setRequests(List<HttpRequest> requests) {
        this.requests = requests;
    }

    /**
     * Checks if requests should be run sequentially.
     *
     * @return true if requests run sequentially, false for parallel execution
     */
    public boolean isRunSequentially() {
        return runSequentially;
    }

    /**
     * Sets whether requests should be run sequentially.
     *
     * @param runSequentially true for sequential, false for parallel execution
     */
    public void setRunSequentially(boolean runSequentially) {
        this.runSequentially = runSequentially;
    }

    /**
     * Gets the delay between sequential requests in milliseconds.
     *
     * @return delay in milliseconds
     */
    public int getDelayBetweenRequests() {
        return delayBetweenRequests;
    }

    /**
     * Sets the delay between sequential requests in milliseconds.
     *
     * @param delayBetweenRequests delay in milliseconds
     */
    public void setDelayBetweenRequests(int delayBetweenRequests) {
        this.delayBetweenRequests = delayBetweenRequests;
    }
}