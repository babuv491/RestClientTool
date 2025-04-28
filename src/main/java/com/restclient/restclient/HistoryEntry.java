package com.restclient.restclient;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class HistoryEntry {
        private final String method;
        private final String url;
        private final String timestamp;

        public HistoryEntry(String method, String url) {
            this.method = method;
            this.url = url;
            this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }

    public String getMethod() {
        return method;
    }

    public String getUrl() {
        return url;
    }

    public String getTimestamp() {
        return timestamp;
    }

    @Override
        public String toString() {
            return String.format("[%s] %s - %s", method, url, timestamp);
        }
    }