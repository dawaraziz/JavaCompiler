package com.project.Parser;

public class Symbol {
    private final String value;
    private final String type;

    public Symbol (final String value, final String type) {
        this.value = value;
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public String getType() {
        return type;
    }
}
