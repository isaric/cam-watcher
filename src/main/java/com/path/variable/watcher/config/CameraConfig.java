package com.path.variable.watcher.config;

import com.path.variable.watcher.monitors.MonitorType;
import com.path.variable.watcher.recorders.RecorderType;

import java.util.List;
import java.util.Map;

/**
 * RecorderConfig - the configuration POJO that is read as a JSON file
 * from the configuration drop-in folder.
 * List of properties:
 *  path - the url of the camera stream
 *  deviceId - the v4l device id (either this or path must be present)
 *  location - the name of the location (mandatory)
 *  areaMinimum - a double specifying the minimum area of movement that must be detected for an alert to be triggered
 *  120.00 is the global default
 *  retries - (optional) the number of retries for reading from the camera, defaults to 1
 *  printTimestamp - whether to add a timestamp to the recording frames
 *  notifiers - the list of notifiers that will be used upon detecting a motion event
 */
public class CameraConfig {

    private String path;

    private Integer deviceId;

    private String location;

    private Double areaMinimum;

    private Integer retries;

    private boolean printTimestamp;

    private List<Map<String, Object>> notifiers;

    private String rtspUrl;

    private RecorderType recorderType;

    private MonitorType monitorType;

    public String getPath() {
        return path;
    }

    public CameraConfig setPath(String path) {
        this.path = path;
        return this;
    }

    public Integer getDeviceId() {
        return deviceId;
    }

    public CameraConfig setDeviceId(Integer deviceId) {
        this.deviceId = deviceId;
        return this;
    }

    public String getLocation() {
        return location;
    }

    public CameraConfig setLocation(String location) {
        this.location = location;
        return this;
    }

    public Double getAreaMinimum() {
        return areaMinimum;
    }

    public CameraConfig setAreaMinimum(Double areaMinimum) {
        this.areaMinimum = areaMinimum;
        return this;
    }

    public Integer getRetries() {
        return retries;
    }

    public CameraConfig setRetries(Integer retries) {
        this.retries = retries;
        return this;
    }

    public List<Map<String, Object>> getNotifiers() {
        return notifiers;
    }

    public CameraConfig setNotifiers(List<Map<String, Object>> notifiers) {
        this.notifiers = notifiers;
        return this;
    }

    public boolean isPrintTimestamp() {
        return printTimestamp;
    }

    public CameraConfig setPrintTimestamp(boolean printTimestamp) {
        this.printTimestamp = printTimestamp;
        return this;
    }

    public String getRtspUrl() {
        return rtspUrl;
    }

    public CameraConfig setRtspUrl(String rtspUrl) {
        this.rtspUrl = rtspUrl;
        return this;
    }

    public RecorderType getRecorderType() {
        return recorderType;
    }

    public CameraConfig setRecorderType(RecorderType recorderType) {
        this.recorderType = recorderType;
        return this;
    }

    public MonitorType getMonitorType() {
        return monitorType;
    }

    public CameraConfig setMonitorType(MonitorType monitorType) {
        this.monitorType = monitorType;
        return this;
    }
}
