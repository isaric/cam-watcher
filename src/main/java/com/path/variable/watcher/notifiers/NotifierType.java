package com.path.variable.watcher.notifiers;

import java.util.Arrays;

public enum NotifierType {
    SLACK,
    NO_OP;

    public static NotifierType getTypeByValue(String value) {
        return Arrays.stream(values())
                     .filter(val -> val.toString().equalsIgnoreCase(value))
                     .findFirst()
                     .orElse(NO_OP);
    }
}
