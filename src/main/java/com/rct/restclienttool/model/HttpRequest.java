package com.rct.restclienttool.model;

import com.rct.restclienttool.AuthType;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
* Represents an HTTP request with all necessary components including
* URL, method, headers, body, authentication, and query parameters.
*
 * @author REST Client Tool
* @version 1.0
* @since 1.0
*/
public class HttpRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    private String url;
    private String method;
    private Map<String, String> headers;
    private String body;
    private AuthType authType;
    private Map<String, String> authParams;
    private Map<String, String> queryParams;
    private String name;

    /**
     * Default constructor that initializes the request with default values.
     * Sets method to GET, auth type to NONE, and initializes empty collections.
     */
    public HttpRequest() {
        this.headers = new HashMap<>();
        this.authParams = new HashMap<>();
        this.queryParams = new HashMap<>();
        this.authType = AuthType.NONE;
        this.method = "GET";
        this.name = "Untitled Request";
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

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    /**
     * Adds a header to the request.
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

    public AuthType getAuthType() {
        return authType;
    }

    public void setAuthType(AuthType authType) {
        this.authType = authType;
    }

    public Map<String, String> getAuthParams() {
        return authParams;
    }

    public void setAuthParams(Map<String, String> authParams) {
        this.authParams = authParams;
    }

    /**
     * Adds an authentication parameter.
     *
     * @param key the parameter name
     * @param value the parameter value
     */
    public void addAuthParam(String key, String value) {
        this.authParams.put(key, value);
    }
   
    public String getName() {
        return name;
    }
   
    public void setName(String name) {
        this.name = name;
    }
   
    public Map<String, String> getQueryParams() {
        return queryParams;
    }
   
    public void setQueryParams(Map<String, String> queryParams) {
        this.queryParams = queryParams;
    }
   
    /**
     * Adds a query parameter to the request.
     *
     * @param key the parameter name
     * @param value the parameter value
     */
    public void addQueryParam(String key, String value) {
        this.queryParams.put(key, value);
    }
}