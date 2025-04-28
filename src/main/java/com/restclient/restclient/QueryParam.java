package com.restclient.restclient;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class QueryParam {
        private final StringProperty key = new SimpleStringProperty();
        private final StringProperty value = new SimpleStringProperty();

        public QueryParam(String key, String value) {
            setKey(key);
            setValue(value);
        }

        public String getKey() { return key.get(); }
        public void setKey(String value) { key.set(value); }
        public StringProperty keyProperty() { return key; }

        public String getValue() { return value.get(); }
        public void setValue(String value) { this.value.set(value); }
        public StringProperty valueProperty() { return value; }
    }