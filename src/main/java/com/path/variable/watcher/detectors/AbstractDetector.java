package com.path.variable.watcher.detectors;

import java.util.Map;

public abstract class AbstractDetector {

    private final Map<String, Object> parameters;

    public AbstractDetector(Map<String, Object> parameters) {
        this.parameters = parameters;
    }

    public abstract boolean detect();

    protected <T> T get(String key) {
        return (T) parameters.get(key);
    }
}
