package com.rct.restclienttool;

import java.io.Serializable;

/**
 * Enumeration of supported authentication types for HTTP requests.
 * Each type has a display name for UI presentation.
 *
 * @author REST Client Tool
 * @version 1.0
 * @since 1.0
 */
public enum AuthType implements Serializable {
    NONE("None"),
    BASIC("Basic Auth"),
    BEARER("Bearer Token"),
    API_KEY("API Key"),
    OAUTH2("OAuth 2.0");

    private final String displayName;

    /**
     * Constructor for AuthType enum values.
     *
     * @param displayName the human-readable display name
     */
    AuthType(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Gets the display name for UI presentation.
     *
     * @return the display name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Returns the display name for string representation.
     *
     * @return the display name
     */
    @Override
    public String toString() {
        return displayName;
    }
}
