package com.path.variable.picam.properties;

import java.util.List;
import java.util.Map;

public class RecorderConfig {

    private String path;

    private Integer deviceId;

    private String location;

    private Double areaMinimum;

    private List<Map<String, Object>> notifiers;

    public String getPath() {
        return path;
    }

    public RecorderConfig setPath(String path) {
        this.path = path;
        return this;
    }

    public Integer getDeviceId() {
        return deviceId;
    }

    public RecorderConfig setDeviceId(Integer deviceId) {
        this.deviceId = deviceId;
        return this;
    }

    public String getLocation() {
        return location;
    }

    public RecorderConfig setLocation(String location) {
        this.location = location;
        return this;
    }

    public Double getAreaMinimum() {
        return areaMinimum;
    }

    public RecorderConfig setAreaMinimum(Double areaMinimum) {
        this.areaMinimum = areaMinimum;
        return this;
    }

    public List<Map<String, Object>> getNotifiers() {
        return notifiers;
    }

    public RecorderConfig setNotifiers(List<Map<String, Object>> notifiers) {
        this.notifiers = notifiers;
        return this;
    }
}
