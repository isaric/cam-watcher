package com.path.variable.watcher.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
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

    private final Integer deviceId;

    private final String location;

    private final Double areaMinimum;

    private final Integer retries;

    private final boolean printTimestamp;

    private final List<Map<String, Object>> notifiers;

    private final String rtspUrl;

    private final RecorderType recorderType;

    private final MonitorType monitorType;

    private final Integer captureDuration;

    @JsonCreator
    public CameraConfig(@JsonProperty("deviceId") Integer deviceId, @JsonProperty("location") String location, @JsonProperty("areaMinimum") Double areaMinimum, @JsonProperty("retries") Integer retries,
                        @JsonProperty("printTimestamp") boolean printTimestamp, @JsonProperty("notifiers") List<Map<String, Object>> notifiers, @JsonProperty("rtspUrl") String rtspUrl,
                        @JsonProperty("recorderType") RecorderType recorderType, @JsonProperty("monitorType") MonitorType monitorType, @JsonProperty("captureDuration") Integer captureDuration) {
        this.deviceId = deviceId;
        this.location = location;
        this.areaMinimum = areaMinimum;
        this.retries = retries;
        this.printTimestamp = printTimestamp;
        this.notifiers = notifiers;
        this.rtspUrl = rtspUrl;
        this.recorderType = recorderType;
        this.monitorType = monitorType;
        this.captureDuration = captureDuration;
    }

    public Integer getDeviceId() {
        return deviceId;
    }

    public String getLocation() {
        return location;
    }

    public Double getAreaMinimum() {
        return areaMinimum;
    }

    public Integer getRetries() {
        return retries;
    }

    public List<Map<String, Object>> getNotifiers() {
        return notifiers;
    }

    public boolean isPrintTimestamp() {
        return printTimestamp;
    }

    public String getRtspUrl() {
        return rtspUrl;
    }

    public RecorderType getRecorderType() {
        return recorderType;
    }

    public MonitorType getMonitorType() {
        return monitorType;
    }

    public Integer getCaptureDuration() {
        return captureDuration;
    }
}
