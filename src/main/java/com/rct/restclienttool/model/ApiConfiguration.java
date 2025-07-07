package com.rct.restclienttool.model;

import com.rct.restclienttool.AuthType;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
* Represents a saved API configuration that can be persisted and reloaded.
* Contains all necessary information to recreate an HTTP request including
* URL, method, headers, body, and authentication details.
*
 * @author REST Client Tool
* @version 1.0
* @since 1.0
*/
public class ApiConfiguration implements Serializable {
    private static final long serialVersionUID = 1L;
   
    private String name;
    private String url;
    private String method;
    private Map<String, String> headers;
    private String body;
    private AuthType authType;
    private Map<String, String> authParams;

    /**
     * Default constructor that initializes the configuration with default values.
     * Sets method to GET, auth type to NONE, and initializes empty collections.
     */
    public ApiConfiguration() {
        this.headers = new HashMap<>();
        this.authParams = new HashMap<>();
        this.authType = AuthType.NONE;
        this.method = "GET";
    }

    /**
     * Gets the configuration name.
     *
     * @return the configuration name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the configuration name.
     *
     * @param name the configuration name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the API endpoint URL.
     *
     * @return the API endpoint URL
     */
    public String getUrl() {
        return url;
    }

    /**
     * Sets the API endpoint URL.
     *
     * @param url the API endpoint URL
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * Gets the HTTP method.
     *
     * @return the HTTP method (GET, POST, PUT, DELETE, etc.)
     */
    public String getMethod() {
        return method;
    }

    /**
     * Sets the HTTP method.
     *
     * @param method the HTTP method (GET, POST, PUT, DELETE, etc.)
     */
    public void setMethod(String method) {
        this.method = method;
    }

    /**
     * Gets the HTTP headers map.
     *
     * @return map of header names to values
     */
    public Map<String, String> getHeaders() {
        return headers;
    }

    /**
     * Sets the HTTP headers map.
     *
     * @param headers map of header names to values
     */
    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    /**
     * Gets the request body content.
     *
     * @return the request body content
     */
    public String getBody() {
        return body;
    }

    /**
     * Sets the request body content.
     *
     * @param body the request body content
     */
    public void setBody(String body) {
        this.body = body;
    }

    /**
     * Gets the authentication type.
     *
     * @return the authentication type
     */
    public AuthType getAuthType() {
        return authType;
    }

    /**
     * Sets the authentication type.
     *
     * @param authType the authentication type
     */
    public void setAuthType(AuthType authType) {
        this.authType = authType;
    }

    /**
     * Gets the authentication parameters.
     *
     * @return map of authentication parameter names to values
     */
    public Map<String, String> getAuthParams() {
        return authParams;
    }

    /**
     * Sets the authentication parameters.
     *
     * @param authParams map of authentication parameter names to values
     */
    public void setAuthParams(Map<String, String> authParams) {
        this.authParams = authParams;
    }

    /**
     * Returns the configuration name for display purposes.
     *
     * @return the configuration name
     */
    @Override
    public String toString() {
        return name;
    }
}