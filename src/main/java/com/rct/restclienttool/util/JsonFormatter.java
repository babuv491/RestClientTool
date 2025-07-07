package com.rct.restclienttool.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
* Utility class for JSON formatting and validation operations.
* Provides methods to pretty-print JSON and validate JSON syntax.
*
 * @author REST Client Tool
* @version 1.0
* @since 1.0
*/
public class JsonFormatter {
    private static final Logger LOGGER = Logger.getLogger(JsonFormatter.class.getName());
    private static final ObjectMapper MAPPER = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    /**
     * Pretty-prints a JSON string with proper indentation.
     *
     * @param json the JSON string to format
     * @return formatted JSON string, or original string if formatting fails
     */
    public static String prettyPrint(String json) {
        if (json == null || json.trim().isEmpty()) {
            return "";
        }
       
        try {
            JsonNode jsonNode = MAPPER.readTree(json);
            return MAPPER.writeValueAsString(jsonNode);
        } catch (JsonProcessingException e) {
            LOGGER.log(Level.WARNING, "Failed to format JSON", e);
            return json; // Return original if formatting fails
        }
    }

    /**
     * Validates if a string is valid JSON.
     *
     * @param json the string to validate
     * @return true if the string is valid JSON, false otherwise
     */
    public static boolean isValidJson(String json) {
        if (json == null || json.trim().isEmpty()) {
            return false;
        }
       
        try {
            MAPPER.readTree(json);
            return true;
        } catch (JsonProcessingException e) {
            return false;
        }
    }
}